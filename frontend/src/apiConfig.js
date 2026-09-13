// 集中管理後端 API 的網址。
// 本機開發沒有設定環境變數時，預設打 http://localhost:8443/movie。
// 部署到正式環境時，只要設定 REACT_APP_API_BASE_URL 這個環境變數，
// 不用再改任何一個元件的程式碼。
export const API_BASE_URL =
    process.env.REACT_APP_API_BASE_URL || 'http://localhost:8443/movie';