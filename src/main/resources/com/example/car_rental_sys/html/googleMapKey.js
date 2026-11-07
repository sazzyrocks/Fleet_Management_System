// ==============================================================================
// Google Maps API Key - Injected dynamically at runtime from .env
// DO NOT hardcode real secrets or API keys in this file.
// Configure GOOGLE_MAPS_API_KEY in your local .env file instead.
// ==============================================================================
let googleMapKey = (typeof window !== "undefined" && window.googleMapKey) ? window.googleMapKey : "";
