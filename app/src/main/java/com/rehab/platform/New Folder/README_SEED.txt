🌱 Database Seeding Script

This script will populate your database with sample data for testing:

WHAT IT CREATES:
✅ 5 Categories (Upper Body, Lower Body, Core, Flexibility, Balance)
✅ 12 Videos (various exercises with descriptions and instructions)
✅ 5 Sample Patients (with realistic data)
✅ 50-100 Progress Records (random completions)
✅ 15-50 Schedules (past and future)

INSTALLATION:
1. Copy the script to your backend:
   cp seed-database.js /home/Nuwu/Downloads/backend/

2. Run the script:
   cd /home/Nuwu/Downloads/backend
   node seed-database.js

3. Wait for completion message

PATIENT LOGIN CREDENTIALS:
Email: john.doe@email.com - Password: patient123
Email: sarah.smith@email.com - Password: patient123
Email: mike.johnson@email.com - Password: patient123
Email: emily.brown@email.com - Password: patient123
Email: david.wilson@email.com - Password: patient123

EXISTING USERS:
Your existing admin and expert accounts will NOT be deleted.
Only videos, categories, schedules, and progress will be cleared and recreated.

WHAT TO EXPECT:
- Analytics dashboard will show real data
- Reports page will have patient records
- Videos page will have 12 exercises
- Categories page will have 5 categories
- Each patient has 5-20 completed exercises
- Each patient has 3-10 scheduled sessions

NOTES:
- Video URLs are placeholders (example.com)
- Thumbnails use placeholder images
- All dates are realistic (within last 60 days for progress)
- Schedules include past and future dates
- 70% of past schedules are marked as completed
