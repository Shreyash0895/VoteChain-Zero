# FreelanceHub 🚀

A production-ready full-stack freelance marketplace — clients post projects, freelancers bid and get hired, both chat privately, share files, use AI tools, join video meetings, and get paid securely with escrow protection.

---

## 🌐 Live Deployment

| Service | URL |
|---------|-----|
| **Frontend** | [https://freelancehub-frontend.onrender.com](https://freelancehub-frontend.onrender.com) |
| **Backend API** | [https://freelancehub-backend.onrender.com](https://freelancehub-backend.onrender.com) |
| **Database** | MongoDB Atlas (Cloud) |
| **Repository** | [github.com/Shreyash0895/Freelancer-Web-App](https://github.com/Shreyash0895/Freelancer-Web-App) |

> 👉 Sign up as a **Client** to post a project, or as a **Freelancer** to start bidding.

---

## ✨ Features

| Feature | Status |
|---------|--------|
| 🏠 Public landing page with live activity ticker | ✅ |
| 🔐 JWT Authentication — Client / Freelancer roles | ✅ |
| 📋 Post, browse, search and filter projects | ✅ |
| 💰 Bidding system with accept / reject | ✅ |
| 🤖 AI proposal generator (Claude) | ✅ |
| 🤖 AI project estimation (Claude) | ✅ |
| 💬 Private chat per project (auto-unlocks on bid acceptance) | ✅ |
| 🌐 Global public chat room | ✅ |
| 📹 Video meetings (Daily.co) | ✅ |
| 💳 Stripe payment processing | ✅ |
| ✅ Payment status saved in MongoDB | ✅ |
| 🔄 Project completion tracking | ✅ |
| 💰 Escrow simulation | ✅ |
| 📧 Email notifications (Nodemailer) | ✅ |
| 🔔 In-app notification bell with live badge | ✅ |
| 📎 File attachments (Cloudinary) | ✅ |
| 📊 Analytics dashboard (Recharts) | ✅ |
| 🧾 PDF invoice download (jsPDF) | ✅ |
| 👤 Profile management with skill tags | ✅ |
| ⭐ Reviews and ratings | ✅ |
| 💥 Error boundary — no blank screens | ✅ |
| ⚡ Skeleton loading states | ✅ |
| 📱 Mobile responsive | ✅ |

---

## 🛠️ Tech Stack

### Frontend
| Tech | Purpose |
|------|---------|
| React 18 + Vite 5 | UI framework and build tool |
| React Router v6 | Client-side routing |
| Axios | HTTP requests with JWT interceptor |
| Socket.io Client | Real-time chat |
| Stripe.js | Payment card UI |
| Recharts | Analytics charts |
| jsPDF + autoTable | PDF invoice generation |
| Framer Motion | Animations |

### Backend
| Tech | Purpose |
|------|---------|
| Node.js + Express | REST API |
| MongoDB Atlas + Mongoose | Database |
| Socket.io | Real-time WebSocket with room auth |
| JWT | Authentication |
| bcryptjs | Password hashing |
| Stripe | Payment gateway |
| Nodemailer | Email via Gmail SMTP |
| Cloudinary + Multer | File uploads |
| Anthropic SDK | AI features (Claude) |
| Joi | Request validation |
| Helmet + Rate Limit | Security |

### Infrastructure
| Service | Purpose |
|---------|---------|
| MongoDB Atlas | Cloud database |
| Render | Hosting — frontend + backend |
| Cloudinary | File and image storage |
| Stripe | Payment processing |
| Gmail SMTP | Email delivery |
| Daily.co | Video meeting rooms |
| Anthropic Claude API | AI proposal + estimation |
| GitHub | Version control + CI/CD |

---

## 🚀 Getting Started Locally

### Prerequisites
- Node.js v18+
- MongoDB Atlas account
- Stripe account (test mode)
- Cloudinary account
- Gmail with App Password
- Anthropic API key
- Daily.co account

### 1. Clone

```bash
git clone https://github.com/Shreyash0895/Freelancer-Web-App.git
cd Freelancer-Web-App
```

### 2. Backend Setup

```bash
cd backend
npm install
```

Create `backend/.env`:
```bash
JWT_SECRET=your_secret_min_32_chars
MONGO_URI=mongodb://username:password@host/freelancer-app?ssl=true&...
PORT=5001
STRIPE_SECRET=sk_test_your_key
EMAIL_USER=yourgmail@gmail.com
EMAIL_PASS=your_16_char_app_password
CLOUDINARY_CLOUD_NAME=your_name
CLOUDINARY_API_KEY=your_key
CLOUDINARY_API_SECRET=your_secret
ANTHROPIC_API_KEY=sk-ant-your_key
DAILY_API_KEY=your_daily_key
FRONTEND_URL=http://localhost:5173
```

```bash
npm run dev   # runs on http://localhost:5001
```

### 3. Frontend Setup

```bash
cd ../frontend
npm install
```

Create `frontend/.env.local`:
```bash
VITE_API_URL=http://localhost:5001
VITE_STRIPE_PUBLIC_KEY=pk_test_your_key
```

```bash
npm run dev   # runs on http://localhost:5173
```

---

## 🔌 API Reference

### Auth
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/signup` | Register + welcome email |
| POST | `/login` | Login → JWT token |

### Profile
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/profile` | My profile |
| PUT | `/profile` | Update profile |
| GET | `/profile/:email` | Public profile + reviews |

### Projects
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/projects` | Paginated + searchable |
| GET | `/projects/:id` | Single project |
| POST | `/projects` | Create project |
| POST | `/projects/:id/pay` | Mark paid in MongoDB |
| POST | `/projects/:id/complete` | Mark completed |

### Bids
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/bid` | Submit bid + email client |
| GET | `/bids/:projectId` | List bids |
| POST | `/accept-bid` | Accept + unlock chat + email freelancer |

### Files
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/projects/:id/upload` | Upload to Cloudinary (10MB max) |
| DELETE | `/projects/:id/files/:i` | Delete file |

### Chat
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/my-chats` | All my conversations |
| GET | `/messages/:room` | Message history |

### Payments + Escrow
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/create-payment` | Stripe payment intent |
| POST | `/escrow/deposit` | Client deposits to escrow |
| POST | `/escrow/release` | Client releases to freelancer |
| GET | `/escrow/:projectId` | Escrow status |

### Reviews
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/reviews` | Submit review |
| GET | `/reviews/:email` | Reviews for freelancer |
| GET | `/reviews/check/:id` | Check if review exists |

### Notifications
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/notifications` | My notifications + unread count |
| PUT | `/notifications/read` | Mark all read |

### AI
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/ai/generate-proposal` | Write bid proposal (Claude) |
| POST | `/ai/estimate-project` | Estimate timeline + budget (Claude) |

### Meetings
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/meetings/create` | Create Daily.co video room |

### Socket.io Events
| Event | Direction | Description |
|-------|-----------|-------------|
| `sendMessage` | Client → Server | Send message |
| `receiveMessage` | Server → Client | Incoming message |
| `chatHistory` | Server → Client | History on join |
| `authError` | Server → Client | Unauthorized room |

---

## 📧 Email Notifications

| Trigger | Recipient | Subject |
|---------|-----------|---------|
| Account signup | New user | Welcome to FreelanceHub 🚀 |
| Bid submitted | Client | New bid on your project 💰 |
| Bid accepted | Freelancer | Your bid was accepted 🎉 |
| File uploaded | Other party | New file uploaded 📎 |
| Payment received | Freelancer | Payment received 💳 |

---

## 🔔 In-App Notification Bell

| Trigger | Recipient | Icon |
|---------|-----------|------|
| New bid | Client | 💰 |
| Bid accepted | Freelancer | 🎉 |
| File uploaded | Other party | 📎 |
| Escrow deposited | Freelancer | 💰 |
| Payment released | Freelancer | 💳 |
| Review received | Freelancer | ⭐ |
| Meeting started | Other party | 📹 |

---

## 🌍 Deployment (Render)

### Backend — Web Service
| Setting | Value |
|---------|-------|
| Root Directory | `backend` |
| Build Command | `npm install` |
| Start Command | `node server.js` |
| Instance Type | Free |
| Variables | All `.env` keys |

### Frontend — Static Site
| Setting | Value |
|---------|-------|
| Root Directory | `frontend` |
| Build Command | `npm install && npm run build` |
| Publish Directory | `dist` |
| Variables | `VITE_API_URL`, `VITE_STRIPE_PUBLIC_KEY` |

### Auto Deploy
Every push to `main` triggers automatic redeploy:
```bash
git add .
git commit -m "your change"
git push
```

### ⚠️ Free Tier Note
Backend sleeps after 15 minutes of inactivity. First request after sleep takes ~30 seconds to wake up. This is normal on Render's free tier.

---

## 💳 Stripe Test Cards

| Card | Result |
|------|--------|
| `4242 4242 4242 4242` | ✅ Payment succeeds |
| `4000 0000 0000 0002` | ❌ Payment declined |
| `4000 0025 0000 3155` | 🔐 3D Secure required |

Any future expiry · Any 3-digit CVC

---

## 🔒 Security

| Layer | Implementation |
|-------|---------------|
| Passwords | bcryptjs — 10 salt rounds |
| Sessions | JWT — expires in 24 hours |
| Auth rate limit | 20 requests / 15 min |
| General rate limit | 100 requests / min |
| HTTP headers | Helmet |
| CORS | Restricted to known origins |
| Private chats | Room verified on REST + Socket.io |
| File uploads | Participants only, 10MB max |
| Error handling | Error boundary — no blank screens |
| Secrets | Never committed to Git |

---

## 👨‍💻 Author

**Shreyash Jokare**
GitHub: [@Shreyash0895](https://github.com/Shreyash0895)



> Built with React, Node.js, MongoDB Atlas, Stripe, Cloudinary, Nodemailer, Claude AI, Daily.co, and Render.