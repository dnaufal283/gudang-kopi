const CACHE_NAME = 'arabica-cache-v1';
const ASSETS_TO_CACHE = [
    '/login',
    '/manifest.json',
    '/images/logo.png',
    'https://cdn.tailwindcss.com',
    'https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@300;400;500;600;700;800&display=swap',
    'https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css',
    'https://cdn.jsdelivr.net/npm/chart.js'
];

// Install Service Worker and cache essential files
self.addEventListener('install', event => {
    event.waitUntil(
        caches.open(CACHE_NAME)
            .then(cache => cache.addAll(ASSETS_TO_CACHE))
            .then(() => self.skipWaiting())
    );
});

// Activate SW and clean up older caches
self.addEventListener('activate', event => {
    event.waitUntil(
        caches.keys().then(keys => {
            return Promise.all(
                keys.map(key => {
                    if (key !== CACHE_NAME) {
                        return caches.delete(key);
                    }
                })
            );
        }).then(() => self.clients.claim())
    );
});

// Fetch Strategy: Network First for transactional pages/APIs, Cache First for static resources
self.addEventListener('fetch', event => {
    const url = new URL(event.request.url);

    // Check if the request is for HTML navigation pages or stats APIs
    if (event.request.mode === 'navigate' || url.pathname.startsWith('/api/')) {
        event.respondWith(
            fetch(event.request)
                .catch(() => {
                    // Fallback to cache if network fails
                    return caches.match(event.request) || caches.match('/login');
                })
        );
    } else {
        // Cache First strategy for static files (CSS, JS, Fonts, Images)
        event.respondWith(
            caches.match(event.request)
                .then(cachedResponse => {
                    if (cachedResponse) {
                        return cachedResponse;
                    }

                    return fetch(event.request).then(response => {
                        // Cache the newly fetched file if it's a successful response
                        if (response && response.status === 200 && response.type === 'basic') {
                            const responseClone = response.clone();
                            caches.open(CACHE_NAME).then(cache => {
                                cache.put(event.request, responseClone);
                            });
                        }
                        return response;
                    }).catch(() => {
                        // Return logo or empty response as offline safety
                        if (url.pathname.endsWith('.png')) {
                            return caches.match('/images/logo.png');
                        }
                    });
                })
        );
    }
});