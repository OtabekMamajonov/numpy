package com.dokonhisob.app.data.repository

import com.dokonhisob.app.data.preferences.SettingsManager
import com.dokonhisob.app.data.remote.ChatCompletionRequest
import com.dokonhisob.app.data.remote.ChatMessage
import com.dokonhisob.app.data.remote.OpenAiService
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.text.NumberFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

sealed class AiResult {
    data class Success(val reply: String) : AiResult()
    data class Error(val message: String) : AiResult()
}

/**
 * Talks to the OpenAI Chat Completions API. Every request is grounded with a
 * fresh summary of the shop's own data (today/week/month totals, top selling
 * products, outstanding debts) so the assistant answers with real numbers
 * instead of guessing.
 */
class AiAssistantRepository(
    private val shopRepository: ShopRepository,
    private val settingsManager: SettingsManager
) {

    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
        .build()

    private val service: OpenAiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.openai.com/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(OpenAiService::class.java)
    }

    suspend fun ask(question: String, history: List<ChatMessage>): AiResult {
        val apiKey = settingsManager.getApiKey()
        if (apiKey.isBlank()) {
            return AiResult.Error("OpenAI API key kiritilmagan. Sozlamalar bo'limiga o'ting va API key qo'shing.")
        }

        val context = buildShopContext()
        val systemPrompt = """
            Siz "${settingsManager.getShopName()}" kichik oziq-ovqat do'koni uchun hisob-kitob yordamchisisiz.
            Faqat quyida berilgan do'kon ma'lumotlari asosida, o'zbek tilida, qisqa va aniq javob bering.
            Pul miqdorlarini "${settingsManager.getCurrency()}" birligida ko'rsating.
            Agar savolga javob berish uchun ma'lumot yetarli bo'lmasa, buni ochiq ayting va taxmin qilmang.
            Do'kon egasiga amaliy maslahatlar berishingiz mumkin (masalan xarajatlarni qisqartirish, qaysi mahsulotni ko'proq zaxira qilish kerakligi).

            Joriy do'kon ma'lumotlari:
            $context
        """.trimIndent()

        val messages = mutableListOf(ChatMessage(role = "system", content = systemPrompt))
        messages.addAll(history)
        messages.add(ChatMessage(role = "user", content = question))

        return try {
            val response = service.createChatCompletion(
                authorization = "Bearer $apiKey",
                request = ChatCompletionRequest(
                    model = settingsManager.getModel(),
                    messages = messages
                )
            )

            val errorMessage = response.error?.message
            if (errorMessage != null) {
                return AiResult.Error(errorMessage)
            }

            val reply = response.choices.firstOrNull()?.message?.content?.trim()
            if (reply.isNullOrBlank()) {
                AiResult.Error("OpenAI'dan javob olinmadi. Qaytadan urinib ko'ring.")
            } else {
                AiResult.Success(reply)
            }
        } catch (e: retrofit2.HttpException) {
            val body = e.response()?.errorBody()?.string()
            AiResult.Error(parseHttpError(e.code(), body))
        } catch (e: java.io.IOException) {
            AiResult.Error("Internet aloqasi topilmadi. Ulanishni tekshirib qayta urinib ko'ring.")
        } catch (e: Exception) {
            AiResult.Error("Xatolik yuz berdi: ${e.message}")
        }
    }

    private fun parseHttpError(code: Int, body: String?): String {
        return when (code) {
            401 -> "OpenAI API key noto'g'ri. Sozlamalarda API key'ni tekshiring."
            429 -> "So'rovlar limiti tugadi yoki hisobingizda mablag' yetarli emas (OpenAI billing'ni tekshiring)."
            else -> "OpenAI xatoligi (kod $code): ${body ?: "noma'lum xato"}"
        }
    }

    private suspend fun buildShopContext(): String {
        val today = ShopRepository.startOfDay() to ShopRepository.endOfDay()
        val week = ShopRepository.daysAgo(7) to ShopRepository.endOfDay()
        val month = ShopRepository.daysAgo(30) to ShopRepository.endOfDay()

        val todaySummary = shopRepository.getSummary(today.first, today.second)
        val weekSummary = shopRepository.getSummary(week.first, week.second)
        val monthSummary = shopRepository.getSummary(month.first, month.second)
        val topProducts = shopRepository.getTopProducts(month.first, month.second, 5)
        val expenseByCategory = shopRepository.getExpensesByCategory(month.first, month.second)
        val debts = shopRepository.getAllCustomerDebts().filter { it.remaining > 0 }

        val fmt = NumberFormat.getNumberInstance(Locale("uz")).apply { maximumFractionDigits = 0 }

        val sb = StringBuilder()
        sb.appendLine("Bugun: savdo=${fmt.format(todaySummary.revenue)}, foyda=${fmt.format(todaySummary.profit)}, xarajat=${fmt.format(todaySummary.expenses)}, sof foyda=${fmt.format(todaySummary.netProfit)}")
        sb.appendLine("Oxirgi 7 kun: savdo=${fmt.format(weekSummary.revenue)}, foyda=${fmt.format(weekSummary.profit)}, xarajat=${fmt.format(weekSummary.expenses)}, sof foyda=${fmt.format(weekSummary.netProfit)}")
        sb.appendLine("Oxirgi 30 kun: savdo=${fmt.format(monthSummary.revenue)}, foyda=${fmt.format(monthSummary.profit)}, xarajat=${fmt.format(monthSummary.expenses)}, sof foyda=${fmt.format(monthSummary.netProfit)}")

        if (topProducts.isNotEmpty()) {
            sb.appendLine("Oxirgi 30 kunda eng ko'p sotilgan mahsulotlar:")
            topProducts.forEach {
                sb.appendLine("- ${it.productName}: ${fmt.format(it.totalQuantity)} dona/kg, tushum ${fmt.format(it.totalRevenue)}")
            }
        }

        if (expenseByCategory.isNotEmpty()) {
            sb.appendLine("Oxirgi 30 kun xarajatlari kategoriya bo'yicha:")
            expenseByCategory.forEach {
                sb.appendLine("- ${it.category}: ${fmt.format(it.total)}")
            }
        }

        if (debts.isNotEmpty()) {
            val totalDebt = debts.sumOf { it.remaining }
            sb.appendLine("Qarzdorlar (jami qoldiq ${fmt.format(totalDebt)}):")
            debts.forEach {
                sb.appendLine("- ${it.customer.name}: ${fmt.format(it.remaining)}")
            }
        } else {
            sb.appendLine("Hozircha qarzdorlar yo'q.")
        }

        return sb.toString()
    }
}
