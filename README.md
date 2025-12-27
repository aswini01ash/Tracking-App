# Tracking-App

# Architecture
Clean Architecture Layers
- Presentation Layer : UI, ViewModel, Service, Worker
- Domain Layer : Use Cases, Models, Repositories
- Data Layer : Room DB, Retrofit, Repositories

# Design Patterns Used
Clean Architecture: Separation of concerns across layers
MVVM: ViewModel manages UI state
Repository Pattern: Abstracts data sources
Dependency Injection: Hilt for DI
Observer Pattern: Flow for reactive updates

#  Features
- Uses FusedLocationProviderClient for efficient location updates
- Collects: latitude, longitude, accuracy, timestamp, speed
- Each location has a sync status flag (isSynced)
- Immediate sync when network restored
- All locations stored locally first

