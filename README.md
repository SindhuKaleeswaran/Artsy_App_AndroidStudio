# Artsy_App_AndroidStudio

A native Android app for discovering artists on Artsy: search by name, open rich artist profiles, browse artworks and categories (“genes”), see similar artists, and manage a favorites list when signed in. Built with Kotlin and Jetpack Compose on the client, backed by an existing Node/Express API that talks to the Artsy platform. The app uses Retrofit + OkHttp for networking, Coil for image loading, and a persistent CookieJar for login sessions.

##Features##

    Splash + App Icon on cold start, using Android’s recommended launcher theme approach.

    Home displays today’s date and a Favorites section:

2.1 Logged out: “Log in to see favorites” button.

2.2 Logged in: a list of favorited artists with name, nationality, birthday, and “added time”; deep-link to details.

    Search from the top bar; typeahead begins after 3 characters and updates results as you type. Results are a scrollable card list with images loaded via Coil; Close clears input.

    Artist Details is a tabbed screen:

4.1 Details: name, nationality, birthday/deathday, biography (omits absent fields).

4.2 Artworks: scrollable list; each card includes title, image, and View categories button opening a dialog. The dialog shows categories in a carousel; shows a loader while fetching.

4.3 Similar: visible only when logged in; scrollable list of similar artists.

    Favorites everywhere: star button on cards and in the details header toggles favorite state with snackbars for feedback; newest-first ordering.

    Authentication: Register, Login, Logout, Delete Account, and “me” session restore. JWT/session cookies are stored via a PersistentCookieJar to keep you signed in across app restarts.

    Resilience: progress indicators during fetch, empty states (“No favorites”, “No artworks”, “No similar artists”), error handling, and fallback to the official Artsy logo when an image is missing.

##Requirements##

Android Studio (latest) with API 34 emulator (Pixel 8 Pro recommended).

Node/Express backend from Artsy_using_Angular_Express(or an equivalent API) running locally or deployed.
