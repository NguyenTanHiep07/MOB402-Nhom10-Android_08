# Architecture — Core, authentication and role navigation

```mermaid
flowchart TB
    subgraph Presentation[Presentation / Jetpack Compose]
        MAIN[MainActivity]
        APP[DeliveryApp]
        LOGIN[XML Login via AndroidView]
        NAV[destinationFor Role]
        CLIENT[ClientHomeScreen]
        DRIVER[DriverHomeScreen]
        ADMIN[AdminHomeScreen]
        DESIGN[GoDrop Theme + Components]
    end

    subgraph State[State holders]
        AUTHVM[AuthViewModel]
        ADMINVM[AdminViewModel]
        DRIVERVM[DriverViewModel]
    end

    subgraph Data[Data layer integrated from feature/database]
        INIT[DatabaseInitializer]
        USERREPO[UserRepository]
        DELIVERYREPO[DeliveryRepository]
        USERDAO[UserDao]
        OTHERDAO[Delivery / Package / History DAO]
        ROOM[(AppDatabase / Room)]
    end

    MAIN --> AUTHVM
    MAIN --> APP
    APP --> LOGIN
    APP --> NAV
    NAV --> CLIENT
    NAV --> DRIVER
    NAV --> ADMIN
    DESIGN -.styles.-> LOGIN
    DESIGN -.styles.-> CLIENT
    DESIGN -.styles.-> DRIVER
    DESIGN -.styles.-> ADMIN

    AUTHVM --> INIT
    AUTHVM --> USERREPO
    ADMIN --> ADMINVM
    DRIVER --> DRIVERVM
    ADMINVM --> USERREPO
    ADMINVM --> DELIVERYREPO
    DRIVERVM --> DELIVERYREPO
    USERREPO --> USERDAO
    DELIVERYREPO --> OTHERDAO
    USERDAO --> ROOM
    OTHERDAO --> ROOM
    INIT --> ROOM
```

## Ranh giới trách nhiệm

- `feature/auth-navigation` sở hữu theme, shared component, Login, auth state và role routing.
- Các feature role sở hữu nội dung bên trong Home tương ứng.
- Data layer cung cấp repository/DAO; auth-navigation chỉ tích hợp qua interface hiện có,
  không định nghĩa lại Entity hoặc nghiệp vụ giao hàng.
