# OMS Mobile App

Angular + Capacitor client that **reuses the same API Gateway contracts** as `web-app`.

## Flow

1. Build the shared Angular UI: `npm --prefix ../web-app run build`
2. Install deps: `npm install`
3. Sync native projects: `npx cap add android` / `npx cap add ios` (once), then `npm run sync`
4. Open in Android Studio / Xcode: `npm run android` / `npm run ios`

Customer order submission (image capture/upload + match confirmation) and admin management screens are the same routes as the web app (`/orders/new`, `/admin`), pointed at the gateway via `environment.apiBaseUrl`.
