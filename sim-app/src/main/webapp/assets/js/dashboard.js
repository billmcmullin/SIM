(() => {
    'use strict';

    // Bootstrap only. All logic lives in dashboard_util/*.js
    if (window.DashboardInit && typeof window.DashboardInit.boot === 'function') {
        window.DashboardInit.boot();
    } else {
        console.error('DashboardInit.boot not found. Ensure dashboard_util/init.js is loaded before dashboard.js');
    }
})();
