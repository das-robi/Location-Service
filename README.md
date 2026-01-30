# Background Location & Weather Tracker App

## Overview

This Android app continuously tracks the device’s location in the background, even when the app is closed or removed from recent apps. The app fetches the current location every 5 minutes, displays it via **Toast messages**, and saves the location data locally using **Room Database**. Additionally, it integrates with the **OpenWeather API** to fetch and display weather information for the current location, using **Geocoder** for location-to-address conversion.

---

## Features

### 1. Background Location Service
- Continuously fetches device location every 5 minutes.
- Works even when:
  - The app is closed.
  - The app is removed from recent apps.
- Displays the fetched location via **Toast messages**.
- Stores each location entry in a **local Room database**.

### 2. Weather API Integration
- Fetches real-time weather data from **OpenWeather API**.
- Converts latitude and longitude to human-readable addresses using **Geocoder**.
- Implements input validation and proper error handling for API responses.
- Displays weather information for the current location.

---

## Technology Stack

- **Android Development:** Java  
- **Background Services:** Foreground Service
- **Database:** RoomDatabase  
- **Networking:** Retrofit
- **API:** OpenWeather API  
- **Geocoding:** Android Geocoder  

