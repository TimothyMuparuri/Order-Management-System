import type { CapacitorConfig } from '@capacitor/cli';

const config: CapacitorConfig = {
  appId: 'com.oms.mobile',
  appName: 'Order MS',
  webDir: '../web-app/dist/web-app/browser',
  server: {
    androidScheme: 'https',
  },
};

export default config;
