import React from 'react'
import ReactDOM from 'react-dom/client'
import App from './App.jsx'
import './styles.css'

const BG = '#050b0a'

const tg = window.Telegram?.WebApp
tg?.ready()
tg?.expand()
// Telegram sarlavhasi ilova foni bilan qo'shilib ketsin
tg?.setHeaderColor?.(BG)
tg?.setBackgroundColor?.(BG)

ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <App />
  </React.StrictMode>
)
