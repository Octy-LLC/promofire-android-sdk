# Installation

```
implementation("io.promofire:promofire:0.2.0")
```

# Setup

```kotlin
Promofire.initialize(applicationContext)

val userInfo = UserInfo(
    customerUserId = "TENANT_ASSIGNED_ID", // optional
    firstName = "FIRST_NAME", // optional
    lastName = "LAST_NAME", // optional
    email = "YOUR_EMAIL", // optional
    phone = "YOUR_PHONE", // optional
)
val promofireConfig = PromofireConfig.Builder("YOUR_SECRET")
    .setUserInfo(userInfo) // optional
    .build()
Promofire.activate(promofireConfig) { result ->
    // Handle result
}
```

# Methods
* is code generation available
  
* get user codes
  
* get code by value
  
* get user redeems
  
* get campaigns
  
* get campaign by id
  
* generate code
  
* generate codes
  
* update code
  
* redeem code
  
* get user
  
* update user
  
* get code redeems
  
* logout
