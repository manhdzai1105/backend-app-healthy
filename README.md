# backend-app-healthy

---

### Over View
A comprehensive health management platform enabling users to manage appointments, interact with the community, and connect via real-time chat.

---

### Technologies

- **Backend:** Spring Boot 3, Java 21, JPA/Hibernate, Liquibase, Redis, PostgreSQL, MapStruct, Spring Security, Quartz, Docker

---

### Features

- **User & Authentication:**  
  Register & login (email/password), Google OAuth2, forgot password (OTP via email), JWT tokens, role-based access, etc.

- **Chat Realtime:**  
  Real-time messaging, video call (WebSocket,WebRTC), etc.

- **Community:**  
  Create & comment on articles, upload images/videos, trending posts, etc.

- **Appointment Management:**  
  Book, reschedule, cancel appointments, etc.

- **Doctor Portal:**  
  Manage patients’s medical records, manage appointment, etc.

- **Payment & Billing:**  
  Integrated with **ZaloPay**, transaction verification, refund handling, etc.

- **Notifications:**  
  Push notifications using **Firebase Cloud Messaging (FCM)**, etc.

- **Admin & Monitoring:**  
  Manage users/doctors, posts, etc.

---

### How to run
- **Clone the repo:**
  ```bash
  git clone https://github.com/manhdzai1105/backend-app-healthy.git
  cd backend-app-healthy
- **Build docker image:**
  ```bash
  docker build -t manh2003ptc/health-management .
- **Set up environment variables (.env):**
  ```bash
  # Create file .env
  DB_HOST=
  DB_PORT=
  DB_NAME=
  DB_USER=
  DB_PASSWORD=
  REDIS_HOST=
  REDIS_PORT=
  REDIS_PASSWORD=
  JWT_ACCESS_SECRET=
  JWT_REFRESH_SECRET=
  JWT_ACCESS_TOKEN_EXPIRATION=
  JWT_REFRESH_TOKEN_EXPIRATION=
  CLOUDINARY_CLOUD_NAME=
  CLOUDINARY_API_KEY=
  CLOUDINARY_API_SECRET=
  SPRING_PROFILES_ACTIVE=prod
  MAIL_HOST=
  MAIL_PORT=
  MAIL_USERNAME=
  MAIL_PASSWORD=
  MINIO_ENDPOINT=
  MINIO_ACCESS_KEY=
  MINIO_SECRET_KEY=
  MINIO_BUCKET=
  MINIO_KEY_PREFIX=
  MINIO_PRESIGN_EXPIRY_SECONDS=
  MINIO_MAKE_BUCKET_PUBLIC=
  ZALOPAY_APP_ID=2553
  ZALOPAY_KEY1=PcY4iZIKFCIdgZvA6ueMcMHHUbRLYjPL
  ZALOPAY_KEY2=kLtgPl8HHhfvMuDHPwKfgfsY4Ydm9eIz
  ZALOPAY_ENDPOINT=https://sb-openapi.zalopay.vn/v2/create
  ZALOPAY_CALLBACK_URL=
  FIREBASE_CREDENTIALS=
  GOOGLE_OAUTH2_CLIENT_ID=
  GOOGLE_OAUTH2_CLIENT_SECRET=
  GOOGLE_OAUTH2_REDIRECT_URI=
  GOOGLE_OAUTH2_ISSUER_URI=
- **Run all services using Docker Compose:**
  ```bash
  docker compose up -d
  
---

### Contact

- **Author:** Dương Văn Mạnh
- **Email:** [duongvanmanh11052003@gmail.com](mailto:duongvanmanh11052003@gmail.com)

