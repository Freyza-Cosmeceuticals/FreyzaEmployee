# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.2.2-5] - 2026-08-30

### Fixed
- **Session Stability**: Fixed unexpected logouts caused by poor or dropping internet connections.
- **Offline Home Screen**: The home screen now loads instantly from the cached session instead of showing a frozen loading skeleton when offline.
- **Connectivity UX**: "Check your internet" messages now appear correctly during slow network timeouts for a better user experience.
- **Duplicate Prevention**: Implemented double-tap protection on the "Save Visit" button to prevent duplicate entry errors.

### Changed
- **UI Performance**: Significantly improved screen loading and tab switching for a smoother experience.

## [0.2.1-4] - 2026-08-23

### Added

- Add a plus icon when creating a new POI for clarity
- **More POIs**: List POIs for multiple locations (both Source and Destination)

### Changed

- **More clear POI names**: Show location name in badge instead of the text itself in POI listing for clarity
- **Better UX with custom POI names**: Do not aggressively clear the POI text field

## [0.2.0-3] - 2026-08-22

### Added
- **In-App Updates**: Fully remote in-app updates for faster iterations.
- **Clearer POI names**: Location name is shown next to POI name in Create Visit and Visit Detail screens.
- **Accessible Samples field**: Sample is accepted if field has some text and focus is lost.

### Changed
- All employees are listed for travelling with for now.

## [0.1.1-2] - 2026-08-09

### Added
- **Travelling With**: Option to select accompanying colleagues during daily report creation.
- **POI Architecture**: Dynamic visit name auto-complete and robust backend synchronization.
- **Offline Resilience**: Network state detection, offline warning banner, and auto-recovery for data fetching.
- **Performance Tracing**: Sentry instrumentation for network calls, UI interactions, and GPS acquisition.
- **Validation UX**: Form fields now instantly highlight specific API validation errors.

## [0.1.0-1] - 2026-07-23

### Added
- **Authentication**: Secure login and session management.
- **Daily Reports**: Create and view daily activity reports.
- **Visit Logging**: Create, view, edit, and manage visits for Doctors, Stockists, and Chemists.
- **Location Tracking**: Added GPS-based visit location tracking with permission and GPS state handling.
- **Travel Plans**: View assigned travel plans and routes for the day.
- **Profile Screen**: Added employee profile information and account settings.
- **Report and Visit Details**: Added detailed views for reports and visits.
- **Currency Handling**: Added centralized currency formatting and money value handling.
- **Clean Architecture**: Implemented Domain, Data, and Presentation layers using Jetpack Compose and Koin DI.

### Changed
- **Add Visit Screen**: Improved the visit creation flow and added support for editing existing visits.
- **Visit Submission**: Editing visits now preserves existing coordinates without unnecessary location refreshes.
- **Location Handling**: Improved location caching, fallback handling, and reliability.
- **UI Architecture**: Improved screen scaffolding, padding handling, loading states, and component organization.
- **Dependency Injection**: Refactored Koin module setup and cleaned up architecture.

### Fixed
- Improved disabled/loading states across forms.
- Fixed scaffold padding and system bar handling issues.
- Improved UI stability and state handling across screens.
