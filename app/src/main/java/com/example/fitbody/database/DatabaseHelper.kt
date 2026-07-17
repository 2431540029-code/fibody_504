package com.example.fitbody.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.fitbody.model.CartItem
import com.example.fitbody.model.CheckIn
import com.example.fitbody.model.Product
import com.example.fitbody.model.Schedule
import com.example.fitbody.model.Trainer
import com.example.fitbody.model.Workout
import com.example.fitbody.model.WorkoutStatsResponse

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "fitbody.db"
        private const val DATABASE_VERSION = 20

        const val TABLE_USERS = "tbl_users"
        const val TABLE_TRAINERS = "tbl_trainers"
        const val TABLE_WORKOUTS = "tbl_workouts"
        const val TABLE_SCHEDULE = "tbl_schedule"
        const val TABLE_PROGRESS = "tbl_progress"
        const val TABLE_CHECKIN = "tbl_checkin"
        const val TABLE_FAVORITES = "tbl_favorites"
        const val TABLE_LIKES = "tbl_likes"
        const val TABLE_PRODUCTS = "products"
        const val TABLE_CART = "cart"
        const val TABLE_REVIEWS = "tbl_reviews"
        const val TABLE_ENROLLMENTS = "tbl_enrollments"
        const val TABLE_ORDERS = "tbl_orders"
        const val TABLE_ORDER_ITEMS = "tbl_order_items"
        const val TABLE_POSTS = "tbl_posts"
        const val TABLE_POST_LIKES = "tbl_post_likes"
        const val TABLE_COMMENTS = "tbl_comments"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS $TABLE_USERS (id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT UNIQUE, password TEXT, email TEXT UNIQUE, social_id TEXT, provider TEXT, role TEXT DEFAULT 'user', avatar TEXT, phone TEXT, address TEXT)")
        db.execSQL("CREATE TABLE IF NOT EXISTS $TABLE_TRAINERS (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, specialty TEXT, muscle TEXT, calories TEXT, schedule_text TEXT, image TEXT, description TEXT, status TEXT DEFAULT 'active', like_count INTEGER DEFAULT 0)")
        db.execSQL("CREATE TABLE IF NOT EXISTS $TABLE_WORKOUTS (id INTEGER PRIMARY KEY AUTOINCREMENT, trainer_id INTEGER, workout_name TEXT, sets_count TEXT, reps_count TEXT, muscle_group TEXT, video_url TEXT, FOREIGN KEY(trainer_id) REFERENCES $TABLE_TRAINERS(id))")
        db.execSQL("CREATE TABLE IF NOT EXISTS $TABLE_SCHEDULE (id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER, day_name TEXT, workout_plan TEXT, is_completed INTEGER DEFAULT 0)")
        db.execSQL("CREATE TABLE IF NOT EXISTS $TABLE_CHECKIN (id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER, checkin_date TEXT, qr_code TEXT)")
        db.execSQL("CREATE TABLE IF NOT EXISTS $TABLE_FAVORITES (id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER, trainer_id INTEGER)")
        db.execSQL("CREATE TABLE IF NOT EXISTS $TABLE_PROGRESS (id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER, weight REAL, height REAL, bmi REAL, date TEXT)")
        db.execSQL("CREATE TABLE IF NOT EXISTS $TABLE_PRODUCTS (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, price INTEGER, original_price INTEGER, image TEXT, description TEXT, category TEXT, stock_status TEXT DEFAULT 'Còn hàng', has_gift INTEGER DEFAULT 0, stock_quantity INTEGER DEFAULT 50, sold_quantity INTEGER DEFAULT 0)")
        db.execSQL("CREATE TABLE IF NOT EXISTS $TABLE_CART (id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER, product_id INTEGER, quantity INTEGER DEFAULT 1, is_selected INTEGER DEFAULT 1, FOREIGN KEY(product_id) REFERENCES $TABLE_PRODUCTS(id))")
        db.execSQL("CREATE TABLE IF NOT EXISTS $TABLE_ORDERS (id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER, total_price INTEGER, order_date TEXT, status TEXT DEFAULT 'Đang xử lý', payment_method TEXT, receiver_name TEXT, receiver_phone TEXT, receiver_address TEXT, estimated_delivery TEXT, refund_reason TEXT, FOREIGN KEY(user_id) REFERENCES $TABLE_USERS(id))")
        db.execSQL("CREATE TABLE IF NOT EXISTS $TABLE_ORDER_ITEMS (id INTEGER PRIMARY KEY AUTOINCREMENT, order_id INTEGER, product_id INTEGER, quantity INTEGER, price INTEGER, FOREIGN KEY(order_id) REFERENCES $TABLE_ORDERS(id), FOREIGN KEY(product_id) REFERENCES $TABLE_PRODUCTS(id))")
        db.execSQL("CREATE TABLE IF NOT EXISTS $TABLE_LIKES (id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER, trainer_id INTEGER, UNIQUE(user_id, trainer_id))")
        db.execSQL("CREATE TABLE IF NOT EXISTS $TABLE_REVIEWS (id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER, trainer_id INTEGER, rating INTEGER, comment TEXT, date TEXT, FOREIGN KEY(user_id) REFERENCES $TABLE_USERS(id), FOREIGN KEY(trainer_id) REFERENCES $TABLE_TRAINERS(id))")
        db.execSQL("CREATE TABLE IF NOT EXISTS $TABLE_ENROLLMENTS (id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER, trainer_id INTEGER, enroll_date TEXT, status TEXT DEFAULT 'active', UNIQUE(user_id, trainer_id), FOREIGN KEY(user_id) REFERENCES $TABLE_USERS(id), FOREIGN KEY(trainer_id) REFERENCES $TABLE_TRAINERS(id))")
        db.execSQL("CREATE TABLE IF NOT EXISTS $TABLE_POSTS (id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER, content TEXT, image TEXT, post_date TEXT, FOREIGN KEY(user_id) REFERENCES $TABLE_USERS(id))")
        db.execSQL("CREATE TABLE IF NOT EXISTS $TABLE_POST_LIKES (id INTEGER PRIMARY KEY AUTOINCREMENT, post_id INTEGER, user_id INTEGER, UNIQUE(post_id, user_id))")
        db.execSQL("CREATE TABLE IF NOT EXISTS $TABLE_COMMENTS (id INTEGER PRIMARY KEY AUTOINCREMENT, post_id INTEGER, user_id INTEGER, comment_text TEXT, comment_date TEXT)")

        db.beginTransaction()
        try {
            seedTrainers(db)
            seedWorkouts(db)
            seedProducts(db)
            seedPTAccounts(db)
            db.setTransactionSuccessful()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            db.endTransaction()
        }
    }

    private fun seedPTAccounts(db: SQLiteDatabase) {
        val pts = arrayOf(
            "('anpt', '123456', 'pt', 'HLV AN')",
            "('quynhanhpt', '123456', 'pt', 'HLV Quỳnh Anh')",
            "('tienpt', '123456', 'pt', 'HLV Tiến')",
            "('tript', '123456', 'pt', 'HLV Trí')",
            "('nhipt', '123456', 'pt', 'HLV Nhi')",
            "('tonypt', '123456', 'pt', 'HLV Tony')",
            "('jennypt', '123456', 'pt', 'HLV Jenny')",
            "('minhanhpt', '123456', 'pt', 'HLV Minh Anh')",
            "('baongocpt', '123456', 'pt', 'HLV Bảo Ngọc')",
            "('hoangnampt', '123456', 'pt', 'HLV Hoàng Nam')",
            "('quochuypt', '123456', 'pt', 'HLV Quốc Huy')",
            "('kimchipt', '123456', 'pt', 'HLV Kim Chi')",
            "('tuankietpt', '123456', 'pt', 'HLV Tuấn Kiệt')",
            "('lanhuongpt', '123456', 'pt', 'HLV Lan Hương')"
        )
        for (pt in pts) {
            db.execSQL("INSERT OR IGNORE INTO $TABLE_USERS (username, password, role, email) VALUES $pt")
        }
    }

    private fun seedTrainers(db: SQLiteDatabase) {
        val trainers = arrayOf(
            "(1, 'HLV AN', 'Bodybuilding', 'Ngực - Tay sau', '850 kcal', 'Thứ 2 / 4 / 6', 'pt_an', 'Chuyên gia xây dựng cơ bắp chuyên sâu.', 'active')",
            "(2, 'HLV Quỳnh Anh', 'Fitness Nữ', 'Mông - Đùi', '720 kcal', 'Thứ 3 / 5 / 7', 'pt_quynh_anh', 'Huấn luyện viên chuyên biệt cho nữ giới.', 'active')",
            "(16, 'HLV Tiến', 'Sức mạnh (Strength)', 'Full Body', '900 kcal', 'Hàng ngày', 'pt_tien', 'Tập trung vào các bài tập sức mạnh cơ bản.', 'active')",
            "(17, 'HLV Trí', 'Calisthenics', 'Lưng - Bụng', '650 kcal', 'Thứ 2 / 3 / 5 / 6', 'pt_tri', 'Chuyên gia tập luyện với trọng lượng cơ thể.', 'active')",
            "(18, 'HLV Nhi', 'Yoga & Pilates', 'Toàn thân', '450 kcal', 'Thứ 3 / 5 / CN', 'pt_nhi', 'Giúp bạn tìm lại sự cân bằng cơ thể.', 'active')",
            "(19, 'HLV Tony', 'HIIT', 'Toàn thân', '1000 kcal', 'Mỗi ngày', 'pt_tony', 'Đốt cháy mỡ thừa tối đa.', 'active')",
            "(20, 'HLV Jenny', 'Pilates & Core', 'Bụng - Eo', '750 kcal', 'Thứ 2 / 4 / 6 / 7', 'pt_jenny', 'Chuyên giáo án giảm mỡ bụng nhanh.', 'active')",
            "(21, 'HLV Minh Anh', 'Cardio', 'Tim mạch', '500 kcal', 'Thứ 3 / 5 / 7', 'pt_minh_anh', 'Hướng dẫn kỹ thuật chuẩn cho người mới.', 'active')",
            "(22, 'HLV Bảo Ngọc', 'Fitness Cơ bản', 'Toàn thân', '480 kcal', 'Thứ 2 / 4 / 6', 'pt_bao_ngoc', 'Nhẹ nhàng và hiệu quả cho người mới.', 'active')",
            "(23, 'HLV Hoàng Nam', 'Tăng cơ sâu', 'Ngực - Vai', '880 kcal', 'Thứ 3 / 5 / 7', 'pt_hoang_nam', 'Phát triển hình thể chuẩn mực.', 'active')",
            "(24, 'HLV Quốc Huy', 'Boxing', 'Combat', '950 kcal', 'Hàng ngày', 'pt_quoc_huy', 'Kỹ thuật võ thuật và phản xạ.', 'active')",
            "(25, 'HLV Kim Chi', 'Phục hồi', 'Giãn cơ', '300 kcal', 'Thứ 2 / 4 / 6', 'pt_kim_chi', 'Phục hồi sau chấn thương.', 'active')",
            "(26, 'HLV Tuấn Kiệt', 'Street Workout', 'Toàn thân', '800 kcal', 'Thứ 3 / 5 / 7', 'pt_tuan_kiet', 'Sức mạnh bùng nổ ngoài trời.', 'active')",
            "(27, 'HLV Lan Hương', 'Thể dục nhẹ', 'Toàn thân', '400 kcal', 'Thứ 2 / 4 / 6', 'pt_lan_huong', 'Sức khỏe dẻo dai mỗi ngày.', 'active')"
        )
        for (t in trainers) {
            db.execSQL("INSERT OR IGNORE INTO $TABLE_TRAINERS (id, name, specialty, muscle, calories, schedule_text, image, description, status) VALUES $t")
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 20) {
            db.execSQL("CREATE TABLE IF NOT EXISTS $TABLE_POSTS (id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER, content TEXT, image TEXT, post_date TEXT, FOREIGN KEY(user_id) REFERENCES $TABLE_USERS(id))")
            db.execSQL("CREATE TABLE IF NOT EXISTS $TABLE_POST_LIKES (id INTEGER PRIMARY KEY AUTOINCREMENT, post_id INTEGER, user_id INTEGER, UNIQUE(post_id, user_id))")
            db.execSQL("CREATE TABLE IF NOT EXISTS $TABLE_COMMENTS (id INTEGER PRIMARY KEY AUTOINCREMENT, post_id INTEGER, user_id INTEGER, comment_text TEXT, comment_date TEXT)")
        }
        if (oldVersion < 19) {
            db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
            db.execSQL("DROP TABLE IF EXISTS $TABLE_TRAINERS")
            db.execSQL("DROP TABLE IF EXISTS $TABLE_WORKOUTS")
            db.execSQL("DROP TABLE IF EXISTS $TABLE_SCHEDULE")
            db.execSQL("DROP TABLE IF EXISTS $TABLE_CHECKIN")
            db.execSQL("DROP TABLE IF EXISTS $TABLE_FAVORITES")
            db.execSQL("DROP TABLE IF EXISTS $TABLE_LIKES")
            db.execSQL("DROP TABLE IF EXISTS $TABLE_PROGRESS")
            db.execSQL("DROP TABLE IF EXISTS $TABLE_PRODUCTS")
            db.execSQL("DROP TABLE IF EXISTS $TABLE_CART")
            db.execSQL("DROP TABLE IF EXISTS $TABLE_REVIEWS")
            db.execSQL("DROP TABLE IF EXISTS $TABLE_ENROLLMENTS")
            db.execSQL("DROP TABLE IF EXISTS $TABLE_ORDERS")
            db.execSQL("DROP TABLE IF EXISTS $TABLE_ORDER_ITEMS")
            onCreate(db)
        }
    }

    private fun seedProducts(db: SQLiteDatabase) {
        val products = arrayOf(
            // TRANG 1
            "(1, 'Rule 1 - Pump (30 lần dùng)', 650000, 800000, 'prod_rule1_pump', 'Tăng sức mạnh bùng nổ.', 'Tăng sức mạnh', 'Còn hàng', 1, 50, 12)",
            "(2, 'Whey Gold Standard 5lbs', 1550000, 1800000, 'prod_whey_gold', 'Đạm tinh khiết tăng cơ.', 'Protein', 'Còn hàng', 1, 30, 45)",
            "(3, 'Mutant Mass 5lbs', 950000, 1100000, 'prod_mutant_mass', 'Sữa tăng cân cực nhanh.', 'Tăng cân', 'Còn hàng', 0, 40, 8)",
            "(4, 'Kirkland - Vitamin C', 640000, 750000, 'prod_vitamin_c', 'Hỗ trợ miễn dịch.', 'Sức khỏe', 'Còn hàng', 0, 100, 150)",
            "(5, 'BCAA Amino Energy', 850000, 950000, 'prod_bcaa', 'Tăng năng lượng tập luyện.', 'Phục hồi', 'Còn hàng', 1, 25, 33)",
            "(6, 'Creatine OstroVit', 550000, 650000, 'prod_creatine', 'Tăng sức mạnh cơ bắp.', 'Tăng sức mạnh', 'Còn hàng', 0, 60, 21)",
            
            // TRANG 2
            "(7, 'ISO 100 5lbs', 1950000, 2200000, 'prod_iso100', 'Whey Protein cao cấp nhất.', 'Protein', 'Còn hàng', 1, 15, 10)",
            "(8, 'Lipo 6 Black', 750000, 850000, 'prod_lipo6', 'Viên uống đốt mỡ mạnh.', 'Giảm mỡ', 'Còn hàng', 0, 40, 56)",
            "(9, 'Omega 3 Kirkland', 450000, 550000, 'prod_omega3', 'Tốt cho tim mạch.', 'Sức khỏe', 'Còn hàng', 0, 80, 200)",
            "(10, 'Pre-workout ABE', 790000, 890000, 'prod_abe', 'Kích thích tập luyện.', 'Tăng sức mạnh', 'Còn hàng', 1, 35, 19)",
            "(11, 'Glucosamine 375 viên', 750000, 850000, 'prod_glucosamine', 'Bảo vệ xương khớp.', 'Sức khỏe', 'Còn hàng', 0, 50, 42)",
            "(12, 'Sữa tăng cân Serious Mass', 1350000, 1500000, 'prod_serious_mass', 'Tăng cân nhanh.', 'Tăng cân', 'Còn hàng', 1, 20, 15)",

            // TRANG 3
            "(13, 'Bình lắc Shaker 700ml', 150000, 200000, 'prod_shaker', 'Tiện lợi pha Protein.', 'Phụ kiện', 'Còn hàng', 0, 120, 500)",
            "(14, 'Găng tay tập Gym', 250000, 350000, 'prod_gloves', 'Bảo vệ bàn tay.', 'Phụ kiện', 'Còn hàng', 0, 45, 89)",
            "(15, 'Thảm tập Yoga', 450000, 600000, 'prod_yoga_mat', 'Chống trượt êm ái.', 'Phụ kiện', 'Còn hàng', 0, 30, 24)",
            "(16, 'Đai lưng tập tạ', 550000, 700000, 'prod_belt', 'Bảo vệ cột sống.', 'Phụ kiện', 'Còn hàng', 0, 25, 13)",
            "(17, 'Dây kéo xà Straps', 120000, 180000, 'prod_straps', 'Hỗ trợ cầm nắm tạ.', 'Phụ kiện', 'Còn hàng', 0, 100, 156)",
            "(18, 'Ống đồng bảo vệ chân', 300000, 450000, 'prod_shin_guards', 'Dành cho võ thuật.', 'Phụ kiện', 'Còn hàng', 0, 40, 7)"
        )
        for (p in products) {
            db.execSQL("INSERT OR IGNORE INTO $TABLE_PRODUCTS (id, name, price, original_price, image, description, category, stock_status, has_gift, stock_quantity, sold_quantity) VALUES $p")
        }
    }

    private fun seedWorkouts(db: SQLiteDatabase) {
        val workouts = arrayOf(
            // HLV AN (ID: 1)
            "(100, 1, 'Bật nhảy', '30 giây', '0', 'Khởi động', '')",
            "(101, 1, 'Chống đẩy cao tay', '16 lần', '16', 'Ngực', '')",
            "(102, 1, 'Chống đẩy bằng đầu gối', '12 lần', '12', 'Ngực', '')",
            "(103, 1, 'Chống đẩy', '10 lần', '10', 'Ngực', '')",
            "(104, 1, 'Bench Press', '4 hiệp', '12', 'Ngực', 'https://youtu.be/rT7DgCr-3pg')",
            
            // HLV Quỳnh Anh (ID: 2)
            "(201, 2, 'Squat', '4 hiệp', '15', 'Mông - Đùi', 'https://youtu.be/aclHkVaku9U')",
            "(202, 2, 'Chùng chân', '3 hiệp', '12', 'Đùi sau', 'https://www.youtube.com/watch?v=QOVaHwm-Q6U')",
            "(205, 2, 'Plank bụng', '60 giây', '0', 'Bụng', 'https://www.youtube.com/watch?v=pSHjTRCQxIw')",

            // HLV Tiến (ID: 16)
            "(301, 16, 'Deadlift', '4 hiệp', '8', 'Full Body', 'https://youtu.be/op9kVnSso6Q')",
            "(302, 16, 'Pull up', '3 hiệp', '10', 'Lưng', '')",

            // HLV Trí (ID: 17)
            "(401, 17, 'Muscle up', '3 hiệp', '5', 'Lưng - Bụng', '')",
            "(402, 17, 'L-Sit', '30 giây', '0', 'Bụng', '')",

            // HLV Nhi (ID: 18)
            "(501, 18, 'Chào mặt trời', '5 vòng', '0', 'Toàn thân', '')",
            "(502, 18, 'Tư thế chiến binh', '10 lần', '0', 'Toàn thân', '')",

            // HLV Tony (ID: 19)
            "(601, 19, 'Burpees', '4 hiệp', '15', 'Toàn thân', '')",
            "(602, 19, 'Mountain Climber', '45 giây', '0', 'Toàn thân', '')",

            // HLV Jenny (ID: 20)
            "(701, 20, 'Bicycle Crunch', '3 hiệp', '20', 'Bụng - Eo', '')",
            "(702, 20, 'Leg Raise', '3 hiệp', '15', 'Bụng - Eo', '')",

            // HLV Minh Anh (ID: 21)
            "(801, 21, 'Chạy tại chỗ', '2 phút', '0', 'Tim mạch', '')",
            "(802, 21, 'Jumping Jack', '50 lần', '0', 'Tim mạch', '')",

            // HLV Bảo Ngọc (ID: 22)
            "(901, 22, 'Lunges', '3 hiệp', '12', 'Toàn thân', '')",
            "(902, 22, 'Glute Bridge', '3 hiệp', '20', 'Toàn thân', '')",

            // HLV Hoàng Nam (ID: 23)
            "(1001, 23, 'Incline Bench Press', '4 hiệp', '10', 'Ngực', '')",
            "(1002, 23, 'Lateral Raise', '3 hiệp', '15', 'Vai', '')",

            // HLV Quốc Huy (ID: 24)
            "(1101, 24, 'Jab-Cross', '3 phút', '0', 'Combat', '')",
            "(1102, 24, 'Hook-Upper', '3 phút', '0', 'Combat', '')",

            // HLV Kim Chi (ID: 25)
            "(1201, 25, 'Căng cơ cổ', '1 phút', '0', 'Giãn cơ', '')",
            "(1202, 25, 'Căng cơ lưng', '1 phút', '0', 'Giãn cơ', '')",

            // HLV Tuấn Kiệt (ID: 26)
            "(1301, 26, 'Dips xà đơn', '4 hiệp', '12', 'Toàn thân', '')",
            "(1302, 26, 'Chin up', '4 hiệp', '10', 'Toàn thân', '')",

            // HLV Lan Hương (ID: 27)
            "(1401, 27, 'Xoay khớp', '3 phút', '0', 'Toàn thân', '')",
            "(1402, 27, 'Lắc hông', '2 phút', '0', 'Toàn thân', '')"
        )
        for (w in workouts) {
            db.execSQL("INSERT OR IGNORE INTO $TABLE_WORKOUTS (id, trainer_id, workout_name, sets_count, reps_count, muscle_group, video_url) VALUES $w")
        }
    }

    fun getUserRole(username: String): String {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT role FROM $TABLE_USERS WHERE username = ?", arrayOf(username))
        var role = "user"
        if (cursor.moveToFirst()) role = cursor.getString(0)
        cursor.close()
        return role
    }

    fun checkUser(username: String, password: String): Int {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT id FROM $TABLE_USERS WHERE username = ? AND password = ?", arrayOf(username, password))
        var id = -1
        if (cursor.moveToFirst()) id = cursor.getInt(0)
        cursor.close()
        return id
    }

    fun getAllTrainers(userId: Int): List<Trainer> {
        val list = mutableListOf<Trainer>()
        val query = "SELECT t.*, (SELECT 1 FROM $TABLE_LIKES l WHERE l.trainer_id = t.id AND l.user_id = ?) as is_liked FROM $TABLE_TRAINERS t WHERE t.status = 'active'"
        val cursor = readableDatabase.rawQuery(query, arrayOf(userId.toString()))
        if (cursor.moveToFirst()) {
            do { list.add(cursorToTrainer(cursor)) } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun getWorkoutsByTrainer(trainerId: Int): List<Workout> {
        val list = mutableListOf<Workout>()
        val cursor = readableDatabase.rawQuery("SELECT * FROM $TABLE_WORKOUTS WHERE trainer_id = ?", arrayOf(trainerId.toString()))
        if (cursor.moveToFirst()) {
            do {
                list.add(Workout(cursor.getInt(0), cursor.getInt(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getString(5), cursor.getString(6)))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun getTopFavoriteTrainers(userId: Int): List<Trainer> {
        val list = mutableListOf<Trainer>()
        val query = "SELECT t.*, (SELECT 1 FROM $TABLE_LIKES l WHERE l.trainer_id = t.id AND l.user_id = ?) as is_liked FROM $TABLE_TRAINERS t WHERE t.status = 'active' ORDER BY like_count DESC LIMIT 3"
        val cursor = readableDatabase.rawQuery(query, arrayOf(userId.toString()))
        if (cursor.moveToFirst()) {
            do { list.add(cursorToTrainer(cursor)) } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun getRandomTrainers(userId: Int): List<Trainer> {
        val list = mutableListOf<Trainer>()
        val query = "SELECT t.*, (SELECT 1 FROM $TABLE_LIKES l WHERE l.trainer_id = t.id AND l.user_id = ?) as is_liked FROM $TABLE_TRAINERS t WHERE t.status = 'active' ORDER BY RANDOM() LIMIT 6"
        val cursor = readableDatabase.rawQuery(query, arrayOf(userId.toString()))
        if (cursor.moveToFirst()) {
            do { list.add(cursorToTrainer(cursor)) } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    private fun cursorToTrainer(cursor: android.database.Cursor): Trainer {
        return Trainer(
            cursor.getInt(cursor.getColumnIndexOrThrow("id")),
            cursor.getString(cursor.getColumnIndexOrThrow("name")),
            cursor.getString(cursor.getColumnIndexOrThrow("specialty")),
            cursor.getString(cursor.getColumnIndexOrThrow("muscle")),
            cursor.getString(cursor.getColumnIndexOrThrow("calories")),
            cursor.getString(cursor.getColumnIndexOrThrow("schedule_text")),
            cursor.getString(cursor.getColumnIndexOrThrow("image")),
            cursor.getString(cursor.getColumnIndexOrThrow("description")),
            cursor.getInt(cursor.getColumnIndexOrThrow("like_count")),
            cursor.getInt(cursor.getColumnIndexOrThrow("is_liked")) == 1
        )
    }

    fun addLike(userId: Int, trainerId: Int): Boolean {
        val db = writableDatabase
        return try {
            val values = ContentValues().apply { put("user_id", userId); put("trainer_id", trainerId) }
            if (db.insert(TABLE_LIKES, null, values) != -1L) {
                db.execSQL("UPDATE $TABLE_TRAINERS SET like_count = like_count + 1 WHERE id = $trainerId")
                true
            } else false
        } catch (e: Exception) { false }
    }

    fun removeLike(userId: Int, trainerId: Int): Boolean {
        val db = writableDatabase
        val deleted = db.delete(TABLE_LIKES, "user_id = ? AND trainer_id = ?", arrayOf(userId.toString(), trainerId.toString())) > 0
        if (deleted) db.execSQL("UPDATE $TABLE_TRAINERS SET like_count = MAX(0, like_count - 1) WHERE id = $trainerId")
        return deleted
    }

    fun getAllWorkouts(): List<Workout> {
        val list = mutableListOf<Workout>()
        val cursor = readableDatabase.rawQuery("SELECT * FROM $TABLE_WORKOUTS ORDER BY id DESC", null)
        if (cursor.moveToFirst()) {
            do { list.add(Workout(cursor.getInt(0), cursor.getInt(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getString(5), cursor.getString(6))) } while (cursor.moveToNext())
        }
        cursor.close(); return list
    }

    fun getWorkoutCount(): Int {
        val c = readableDatabase.rawQuery("SELECT COUNT(*) FROM $TABLE_WORKOUTS", null)
        var count = 0; if (c.moveToFirst()) count = c.getInt(0); c.close(); return count
    }

    fun getTopFavoriteTrainer(): String {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT t.name FROM $TABLE_TRAINERS t LEFT JOIN $TABLE_FAVORITES f ON t.id = f.trainer_id GROUP BY t.id ORDER BY COUNT(f.id) DESC LIMIT 1", null)
        var name = "Chưa có"; if (cursor.moveToFirst()) name = cursor.getString(0); cursor.close(); return name
    }

    fun addFavorite(userId: Int, trainerId: Int): Boolean {
        return writableDatabase.insert(TABLE_FAVORITES, null, ContentValues().apply { put("user_id", userId); put("trainer_id", trainerId) }) != -1L
    }

    fun getFavorites(userId: Int): List<Trainer> {
        val list = mutableListOf<Trainer>()
        val query = "SELECT t.*, (SELECT 1 FROM $TABLE_LIKES l WHERE l.trainer_id = t.id AND l.user_id = ?) as is_liked FROM $TABLE_TRAINERS t JOIN $TABLE_FAVORITES f ON t.id = f.trainer_id WHERE f.user_id = ?"
        val cursor = readableDatabase.rawQuery(query, arrayOf(userId.toString(), userId.toString()))
        if (cursor.moveToFirst()) {
            do { list.add(cursorToTrainer(cursor)) } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun removeFavorite(userId: Int, trainerId: Int): Boolean = writableDatabase.delete(TABLE_FAVORITES, "user_id = ? AND trainer_id = ?", arrayOf(userId.toString(), trainerId.toString())) > 0

    fun registerUser(username: String, email: String, password: String): Long = writableDatabase.insert(TABLE_USERS, null, ContentValues().apply { put("username", username); put("email", email); put("password", password); put("role", "user") })

    fun getUserProfile(userId: Int): android.database.Cursor = readableDatabase.rawQuery("SELECT username, email, avatar, phone, address FROM $TABLE_USERS WHERE id = ?", arrayOf(userId.toString()))

    fun updateUserProfile(userId: Int, name: String, email: String, avatar: String? = null, phone: String? = null, address: String? = null): Boolean {
        val v = ContentValues().apply { put("username", name); put("email", email)
            if (avatar != null) put("avatar", avatar)
            if (phone != null) put("phone", phone)
            if (address != null) put("address", address)
        }
        return writableDatabase.update(TABLE_USERS, v, "id = ?", arrayOf(userId.toString())) > 0
    }

    fun updateUserAvatar(userId: Int, avatar: String): Boolean {
        val v = ContentValues().apply { put("avatar", avatar) }
        return writableDatabase.update(TABLE_USERS, v, "id = ?", arrayOf(userId.toString())) > 0
    }

    fun getProductsByPage(page: Int, pageSize: Int, category: String = "Tất cả"): List<Product> {
        val list = mutableListOf<Product>()
        val offset = (page - 1) * pageSize
        val query = if (category == "Tất cả") {
            "SELECT * FROM $TABLE_PRODUCTS LIMIT ? OFFSET ?"
        } else {
            "SELECT * FROM $TABLE_PRODUCTS WHERE category LIKE ? LIMIT ? OFFSET ?"
        }
        val args = if (category == "Tất cả") {
            arrayOf(pageSize.toString(), offset.toString())
        } else {
            arrayOf("%$category%", pageSize.toString(), offset.toString())
        }

        val cursor = readableDatabase.rawQuery(query, args)
        if (cursor.moveToFirst()) {
            val idIdx = cursor.getColumnIndex("id")
            val nameIdx = cursor.getColumnIndex("name")
            val priceIdx = cursor.getColumnIndex("price")
            val origIdx = cursor.getColumnIndex("original_price")
            val imgIdx = cursor.getColumnIndex("image")
            val descIdx = cursor.getColumnIndex("description")
            val catIdx = cursor.getColumnIndex("category")
            val statusIdx = cursor.getColumnIndex("stock_status")
            val giftIdx = cursor.getColumnIndex("has_gift")
            val qtyIdx = cursor.getColumnIndex("stock_quantity")
            val soldIdx = cursor.getColumnIndex("sold_quantity")

            do {
                list.add(Product(
                    if (idIdx != -1) cursor.getInt(idIdx) else 0,
                    if (nameIdx != -1) cursor.getString(nameIdx) else "",
                    if (priceIdx != -1) cursor.getInt(priceIdx) else 0,
                    if (origIdx != -1) cursor.getInt(origIdx) else 0,
                    if (imgIdx != -1) cursor.getString(imgIdx) else "",
                    if (descIdx != -1) cursor.getString(descIdx) else "",
                    if (catIdx != -1) cursor.getString(catIdx) else "",
                    if (statusIdx != -1) cursor.getString(statusIdx) == "Còn hàng" else true,
                    if (giftIdx != -1) cursor.getInt(giftIdx) == 1 else false,
                    if (qtyIdx != -1) cursor.getInt(qtyIdx) else 50,
                    if (soldIdx != -1) cursor.getInt(soldIdx) else 0
                ))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun getTotalProductCount(category: String = "Tất cả"): Int {
        val query = if (category == "Tất cả") {
            "SELECT COUNT(*) FROM $TABLE_PRODUCTS"
        } else {
            "SELECT COUNT(*) FROM $TABLE_PRODUCTS WHERE category LIKE ?"
        }
        val args = if (category == "Tất cả") null else arrayOf("%$category%")
        val c = readableDatabase.rawQuery(query, args)
        var count = 0; if (c.moveToFirst()) count = c.getInt(0); c.close(); return count
    }

    fun addToCart(userId: Int, productId: Int, quantity: Int = 1): Boolean {
        val db = writableDatabase
        val c = db.rawQuery("SELECT quantity FROM $TABLE_CART WHERE user_id = ? AND product_id = ?", arrayOf(userId.toString(), productId.toString()))
        return if (c.moveToFirst()) {
            val qty = c.getInt(0) + quantity
            db.update(TABLE_CART, ContentValues().apply { put("quantity", qty) }, "user_id = ? AND product_id = ?", arrayOf(userId.toString(), productId.toString())) > 0
        } else {
            db.insert(TABLE_CART, null, ContentValues().apply { put("user_id", userId); put("product_id", productId); put("quantity", quantity); put("is_selected", 1) }) != -1L
        }.also { c.close() }
    }

    fun getTrainerIdByUsername(username: String): Int {
        val c = readableDatabase.rawQuery("SELECT id FROM $TABLE_TRAINERS WHERE name = (SELECT email FROM $TABLE_USERS WHERE username = ?)", arrayOf(username))
        var id = 0; if (c.moveToFirst()) id = c.getInt(0); c.close(); return id
    }

    fun addWorkout(w: Workout): Boolean = writableDatabase.insert(TABLE_WORKOUTS, null, ContentValues().apply { put("trainer_id", w.trainer_id); put("workout_name", w.workout_name); put("sets_count", w.sets_count); put("reps_count", w.reps_count); put("muscle_group", w.muscle_group); put("video_url", w.video_url) }) != -1L
    fun deleteWorkout(id: Int): Boolean = writableDatabase.delete(TABLE_WORKOUTS, "id = ?", arrayOf(id.toString())) > 0
    fun updateWorkout(w: Workout): Boolean = writableDatabase.update(TABLE_WORKOUTS, ContentValues().apply { put("workout_name", w.workout_name); put("sets_count", w.sets_count); put("reps_count", w.reps_count); put("muscle_group", w.muscle_group); put("video_url", w.video_url) }, "id = ?", arrayOf(w.id.toString())) > 0

    fun getWorkoutById(id: Int): Workout? {
        val c = readableDatabase.rawQuery("SELECT * FROM $TABLE_WORKOUTS WHERE id = ?", arrayOf(id.toString()))
        var w: Workout? = null
        if (c.moveToFirst()) w = Workout(c.getInt(0), c.getInt(1), c.getString(2), c.getString(3), c.getString(4), c.getString(5), c.getString(6))
        c.close(); return w
    }

    fun getCart(userId: Int): List<CartItem> {
        val list = mutableListOf<CartItem>()
        val query = "SELECT c.id, c.product_id, c.quantity, c.is_selected, p.name, p.price, p.image FROM $TABLE_CART c JOIN $TABLE_PRODUCTS p ON c.product_id = p.id WHERE c.user_id = ?"
        val cursor = readableDatabase.rawQuery(query, arrayOf(userId.toString()))
        if (cursor.moveToFirst()) {
            do { list.add(CartItem(cursor.getInt(0), cursor.getInt(1), cursor.getString(4), cursor.getInt(5), cursor.getString(6), cursor.getInt(2), cursor.getInt(3) == 1)) } while (cursor.moveToNext())
        }
        cursor.close(); return list
    }

    fun updateCartSelection(id: Int, s: Boolean): Boolean = writableDatabase.update(TABLE_CART, ContentValues().apply { put("is_selected", if (s) 1 else 0) }, "id = ?", arrayOf(id.toString())) > 0

    fun placeOrder(userId: Int, total: Int, items: List<CartItem>, paymentMethod: String, name: String, phone: String, address: String): Long {
        val db = writableDatabase
        db.beginTransaction()
        try {
            val date = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
            val calendar = java.util.Calendar.getInstance()
            calendar.add(java.util.Calendar.DAY_OF_YEAR, 3)
            val estDate = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(calendar.time)

            val orderId = db.insert(TABLE_ORDERS, null, ContentValues().apply { put("user_id", userId); put("total_price", total); put("order_date", date); put("status", "Đang xử lý"); put("payment_method", paymentMethod); put("receiver_name", name); put("receiver_phone", phone); put("receiver_address", address); put("estimated_delivery", estDate) })
            if (orderId == -1L) return -1L

            for (item in items) {
                db.insert(TABLE_ORDER_ITEMS, null, ContentValues().apply { put("order_id", orderId); put("product_id", item.productId); put("quantity", item.quantity); put("price", item.price) })
                
                // Trừ tồn kho và tăng số lượng đã bán
                db.execSQL("UPDATE $TABLE_PRODUCTS SET stock_quantity = MAX(0, stock_quantity - ?), sold_quantity = sold_quantity + ? WHERE id = ?", 
                    arrayOf(item.quantity, item.quantity, item.productId))

                db.delete(TABLE_CART, "product_id = ? AND user_id = ?", arrayOf(item.productId.toString(), userId.toString()))
            }
            db.setTransactionSuccessful(); return orderId
        } catch (e: Exception) { return -1L } finally { db.endTransaction() }
    }

    fun updateOrderStatus(id: Int, status: String, refundReason: String? = null): Boolean {
        val db = writableDatabase
        // Nếu hủy đơn hoặc hoàn tiền, trả lại số lượng vào kho
        if (status == "Đã hủy" || status == "Yêu cầu hoàn tiền") {
            val items = getOrderItems(id)
            for (item in items) {
                db.execSQL("UPDATE $TABLE_PRODUCTS SET stock_quantity = stock_quantity + ?, sold_quantity = MAX(0, sold_quantity - ?) WHERE id = ?",
                    arrayOf(item.quantity, item.quantity, item.productId))
            }
        }
        return db.update(TABLE_ORDERS, ContentValues().apply { put("status", status); if (refundReason != null) put("refund_reason", refundReason) }, "id = ?", arrayOf(id.toString())) > 0
    }

    fun syncProductsFromServer(products: List<Product>) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            for (p in products) {
                val v = ContentValues().apply {
                    put("name", p.name)
                    put("price", p.price)
                    put("original_price", p.originalPrice)
                    put("image", p.image)
                    put("description", p.description)
                    put("category", p.category)
                    // Không ghi đè stock_quantity để giữ logic kho thật
                }
                db.update(TABLE_PRODUCTS, v, "id = ?", arrayOf(p.id.toString()))
            }
            db.setTransactionSuccessful()
        } finally { db.endTransaction() }
    }

    fun clearAllData() {
        val db = writableDatabase
        db.execSQL("DELETE FROM $TABLE_CART")
        db.execSQL("DELETE FROM $TABLE_ORDERS")
        db.execSQL("DELETE FROM $TABLE_ORDER_ITEMS")
        db.execSQL("DELETE FROM $TABLE_CHECKIN")
        db.execSQL("DELETE FROM $TABLE_PROGRESS")
        db.execSQL("DELETE FROM $TABLE_SCHEDULE")
    }

    fun getOrderById(id: Int): com.example.fitbody.model.Order? {
        val c = readableDatabase.rawQuery("SELECT * FROM $TABLE_ORDERS WHERE id = ?", arrayOf(id.toString()))
        var o: com.example.fitbody.model.Order? = null
        if (c.moveToFirst()) o = com.example.fitbody.model.Order(c.getInt(0), c.getInt(1), c.getInt(2), c.getString(3), c.getString(4), c.getString(5), c.getString(6), c.getString(7), c.getString(8), c.getString(9), c.getString(10), getOrderItems(id))
        c.close(); return o
    }

    fun getOrderHistory(userId: Int): List<com.example.fitbody.model.Order> {
        val list = mutableListOf<com.example.fitbody.model.Order>()
        val cursor = readableDatabase.rawQuery("SELECT * FROM $TABLE_ORDERS WHERE user_id = ? ORDER BY id DESC", arrayOf(userId.toString()))
        if (cursor.moveToFirst()) {
            do {
                val orderId = cursor.getInt(0)
                list.add(com.example.fitbody.model.Order(orderId, cursor.getInt(1), cursor.getInt(2), cursor.getString(3), cursor.getString(4), cursor.getString(5), cursor.getString(6), cursor.getString(7), cursor.getString(8), cursor.getString(9), cursor.getString(10), getOrderItems(orderId)))
            } while (cursor.moveToNext())
        }
        cursor.close(); return list
    }

    fun getOrderItems(orderId: Int): List<com.example.fitbody.model.OrderItem> {
        val list = mutableListOf<com.example.fitbody.model.OrderItem>()
        val cursor = readableDatabase.rawQuery("SELECT oi.*, p.name, p.image FROM $TABLE_ORDER_ITEMS oi JOIN $TABLE_PRODUCTS p ON oi.product_id = p.id WHERE oi.order_id = ?", arrayOf(orderId.toString()))
        if (cursor.moveToFirst()) {
            do { list.add(com.example.fitbody.model.OrderItem(cursor.getInt(0), cursor.getInt(1), cursor.getInt(2), cursor.getString(cursor.getColumnIndexOrThrow("name")), cursor.getString(cursor.getColumnIndexOrThrow("image")), cursor.getInt(3), cursor.getInt(4))) } while (cursor.moveToNext())
        }
        cursor.close(); return list
    }

    fun getSchedule(userId: Int): List<Schedule> {
        val list = mutableListOf<Schedule>()
        val cursor = readableDatabase.rawQuery("SELECT * FROM $TABLE_SCHEDULE WHERE user_id = ?", arrayOf(userId.toString()))
        if (cursor.moveToFirst()) {
            do { list.add(Schedule(cursor.getInt(0), cursor.getInt(1), cursor.getString(2), cursor.getString(3), if (cursor.getInt(4) == 1) "completed" else "pending")) } while (cursor.moveToNext())
        }
        cursor.close(); return list
    }

    fun addSchedule(userId: Int, day: String, plan: String): Long = writableDatabase.insert(TABLE_SCHEDULE, null, ContentValues().apply { put("user_id", userId); put("day_name", day); put("workout_plan", plan); put("is_completed", 0) })
    fun completeSchedule(id: Int): Boolean = writableDatabase.update(TABLE_SCHEDULE, ContentValues().apply { put("is_completed", 1) }, "id = ?", arrayOf(id.toString())) > 0
    fun deleteSchedule(id: Int): Boolean = writableDatabase.delete(TABLE_SCHEDULE, "id = ?", arrayOf(id.toString())) > 0

    fun addCheckIn(userId: Int, qr: String): Long {
        val date = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
        return writableDatabase.insert(TABLE_CHECKIN, null, ContentValues().apply { put("user_id", userId); put("qr_code", qr); put("checkin_date", date) })
    }

    fun getCheckInHistoryList(userId: Int): List<CheckIn> {
        val list = mutableListOf<CheckIn>()
        val cursor = readableDatabase.rawQuery("SELECT * FROM $TABLE_CHECKIN WHERE user_id = ? ORDER BY id DESC", arrayOf(userId.toString()))
        if (cursor.moveToFirst()) {
            do { list.add(CheckIn(cursor.getInt(0), cursor.getInt(1), cursor.getString(2))) } while (cursor.moveToNext())
        }
        cursor.close(); return list
    }

    fun getWorkoutStats(userId: Int): WorkoutStatsResponse {
        val cursor = readableDatabase.rawQuery("SELECT COUNT(*) FROM $TABLE_CHECKIN WHERE user_id = ?", arrayOf(userId.toString()))
        var count = 0; if (cursor.moveToFirst()) count = cursor.getInt(0); cursor.close()
        return WorkoutStatsResponse(true, count, count * 350, if (count > 0) count % 7 + 1 else 0, (count * 100 / 20).coerceAtMost(100))
    }

    fun saveProgress(userId: Int, w: Double, h: Double, bmi: Double): Long {
        val date = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(java.util.Date())
        return writableDatabase.insert(TABLE_PROGRESS, null, ContentValues().apply { put("user_id", userId); put("weight", w); put("height", h); put("bmi", bmi); put("date", date) })
    }

    fun getLatestProgress(userId: Int): android.database.Cursor = readableDatabase.rawQuery("SELECT * FROM $TABLE_PROGRESS WHERE user_id = ? ORDER BY id DESC LIMIT 1", arrayOf(userId.toString()))
    fun updateTrainerImage(id: Int, path: String): Boolean = writableDatabase.update(TABLE_TRAINERS, ContentValues().apply { put("image", path) }, "id = ?", arrayOf(id.toString())) > 0

    fun addReview(userId: Int, trainerId: Int, rating: Int, comment: String): Boolean {
        val date = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(java.util.Date())
        return writableDatabase.insert(TABLE_REVIEWS, null, ContentValues().apply { put("user_id", userId); put("trainer_id", trainerId); put("rating", rating); put("comment", comment); put("date", date) }) != -1L
    }

    fun getReviewsForTrainer(trainerId: Int): List<com.example.fitbody.model.Review> {
        val list = mutableListOf<com.example.fitbody.model.Review>()
        val query = "SELECT r.*, u.username FROM $TABLE_REVIEWS r JOIN $TABLE_USERS u ON r.user_id = u.id WHERE r.trainer_id = ? ORDER BY r.id DESC"
        val cursor = readableDatabase.rawQuery(query, arrayOf(trainerId.toString()))
        if (cursor.moveToFirst()) {
            do { list.add(com.example.fitbody.model.Review(cursor.getInt(0), cursor.getInt(1), cursor.getString(cursor.getColumnIndexOrThrow("username")), cursor.getInt(3), cursor.getInt(4), cursor.getString(5), cursor.getString(6))) } while (cursor.moveToNext())
        }
        cursor.close(); return list
    }

    fun getTrainerStudentCount(trainerId: Int): Int {
        val query = "SELECT COUNT(DISTINCT user_id) FROM (SELECT user_id FROM $TABLE_FAVORITES WHERE trainer_id = ? UNION SELECT user_id FROM $TABLE_REVIEWS WHERE trainer_id = ? UNION SELECT user_id FROM $TABLE_LIKES WHERE trainer_id = ? UNION SELECT user_id FROM $TABLE_ENROLLMENTS WHERE trainer_id = ?)"
        val cursor = readableDatabase.rawQuery(query, arrayOf(trainerId.toString(), trainerId.toString(), trainerId.toString(), trainerId.toString()))
        var count = 0; if (cursor.moveToFirst()) count = cursor.getInt(0); cursor.close(); return count
    }

    fun getLeaderboard(): List<com.example.fitbody.model.LeaderboardUser> {
        val list = mutableListOf<com.example.fitbody.model.LeaderboardUser>()
        val query = "SELECT u.username, (SELECT COUNT(*) FROM $TABLE_CHECKIN WHERE user_id = u.id) as workout_count FROM $TABLE_USERS u WHERE u.role = 'user' ORDER BY workout_count DESC LIMIT 10"
        val cursor = readableDatabase.rawQuery(query, null)
        if (cursor.moveToFirst()) { do { list.add(com.example.fitbody.model.LeaderboardUser(cursor.getString(0), cursor.getInt(1))) } while (cursor.moveToNext()) }
        cursor.close(); return list
    }

    fun enrollTrainer(userId: Int, trainerId: Int): Boolean {
        val date = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(java.util.Date())
        return try { writableDatabase.insertWithOnConflict(TABLE_ENROLLMENTS, null, ContentValues().apply { put("user_id", userId); put("trainer_id", trainerId); put("enroll_date", date) }, SQLiteDatabase.CONFLICT_IGNORE) != -1L } catch (e: Exception) { false }
    }

    fun isUserEnrolled(userId: Int, trainerId: Int): Boolean {
        val cursor = readableDatabase.rawQuery("SELECT 1 FROM $TABLE_ENROLLMENTS WHERE user_id = ? AND trainer_id = ?", arrayOf(userId.toString(), trainerId.toString()))
        val exists = cursor.moveToFirst(); cursor.close(); return exists
    }

    fun unenrollTrainer(userId: Int, trainerId: Int): Boolean = writableDatabase.delete(TABLE_ENROLLMENTS, "user_id = ? AND trainer_id = ?", arrayOf(userId.toString(), trainerId.toString())) > 0

    fun getStudentsForTrainer(trainerId: Int): List<com.example.fitbody.model.User> {
        val list = mutableListOf<com.example.fitbody.model.User>()
        val cursor = readableDatabase.rawQuery("SELECT u.* FROM $TABLE_USERS u JOIN $TABLE_ENROLLMENTS e ON u.id = e.user_id WHERE e.trainer_id = ?", arrayOf(trainerId.toString()))
        if (cursor.moveToFirst()) { do { list.add(com.example.fitbody.model.User(cursor.getInt(cursor.getColumnIndexOrThrow("id")), cursor.getString(cursor.getColumnIndexOrThrow("username")), cursor.getString(cursor.getColumnIndexOrThrow("email")), "", cursor.getString(cursor.getColumnIndexOrThrow("role")))) } while (cursor.moveToNext()) }
        cursor.close(); return list
    }

    fun getUserBySocialId(id: String, p: String): Int {
        val c = readableDatabase.rawQuery("SELECT id FROM $TABLE_USERS WHERE social_id = ? AND provider = ?", arrayOf(id, p))
        var res = -1; if (c.moveToFirst()) res = c.getInt(0); c.close(); return res
    }

    fun getUserById(id: Int): com.example.fitbody.model.User? {
        val cursor = readableDatabase.rawQuery("SELECT * FROM $TABLE_USERS WHERE id = ?", arrayOf(id.toString()))
        return if (cursor.moveToFirst()) {
            val user = com.example.fitbody.model.User(
                cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                cursor.getString(cursor.getColumnIndexOrThrow("username")),
                cursor.getString(cursor.getColumnIndexOrThrow("email")),
                cursor.getString(cursor.getColumnIndexOrThrow("password")),
                cursor.getString(cursor.getColumnIndexOrThrow("role")),
                cursor.getString(cursor.getColumnIndexOrThrow("avatar"))
            )
            cursor.close()
            user
        } else {
            cursor.close()
            null
        }
    }
    fun registerSocialUser(u: String, e: String, sid: String, p: String): Long = writableDatabase.insert(TABLE_USERS, null, ContentValues().apply { put("username", u); put("email", e); put("social_id", sid); put("provider", p); put("role", "user") })

    fun searchTrainers(query: String, userId: Int): List<com.example.fitbody.model.Trainer> {
        val list = mutableListOf<com.example.fitbody.model.Trainer>()
        val sql = "SELECT DISTINCT t.*, (SELECT COUNT(*) FROM $TABLE_LIKES l WHERE l.trainer_id = t.id) as like_count, (SELECT 1 FROM $TABLE_LIKES l WHERE l.trainer_id = t.id AND l.user_id = ?) as is_liked FROM $TABLE_TRAINERS t LEFT JOIN $TABLE_WORKOUTS w ON t.id = w.trainer_id WHERE t.name LIKE ? OR t.specialty LIKE ? OR t.muscle LIKE ? OR w.workout_name LIKE ? OR w.muscle_group LIKE ?"
        val p = "%${query.trim()}%"; val cursor = readableDatabase.rawQuery(sql, arrayOf(userId.toString(), p, p, p, p, p))
        if (cursor.moveToFirst()) { do { list.add(cursorToTrainer(cursor)) } while (cursor.moveToNext()) }
        cursor.close(); return list
    }

    // --- SOCIAL MEDIA FUNCTIONS ---
    fun addPost(userId: Int, content: String, image: String?): Long {
        val date = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
        val v = ContentValues().apply { put("user_id", userId); put("content", content); put("image", image); put("post_date", date) }
        return writableDatabase.insert(TABLE_POSTS, null, v)
    }

    fun getAllPosts(currentUserId: Int): List<com.example.fitbody.model.Post> {
        val list = mutableListOf<com.example.fitbody.model.Post>()
        val sql = "SELECT p.*, u.username, u.avatar, (SELECT COUNT(*) FROM $TABLE_POST_LIKES WHERE post_id = p.id) as likes, (SELECT 1 FROM $TABLE_POST_LIKES WHERE post_id = p.id AND user_id = ?) as is_liked FROM $TABLE_POSTS p JOIN $TABLE_USERS u ON p.user_id = u.id ORDER BY p.id DESC"
        val c = readableDatabase.rawQuery(sql, arrayOf(currentUserId.toString()))
        if (c.moveToFirst()) {
            do {
                val id = c.getInt(0)
                val userId = c.getInt(1)
                val content = c.getString(2) ?: ""
                val image = c.getString(3)
                val date = c.getString(4) ?: ""
                val username = c.getString(6) ?: "User"
                val avatar = c.getString(7)
                val likes = c.getInt(8)
                val isLiked = c.getInt(9) == 1
                
                list.add(com.example.fitbody.model.Post(id, userId, username, avatar, content, image, date, likes, isLiked))
            } while (c.moveToNext())
        }
        c.close(); return list
    }

    fun togglePostLike(userId: Int, postId: Int): Boolean {
        val db = writableDatabase
        val c = db.rawQuery("SELECT id FROM $TABLE_POST_LIKES WHERE post_id = ? AND user_id = ?", arrayOf(postId.toString(), userId.toString()))
        val exists = c.moveToFirst()
        c.close()
        return if (exists) {
            db.delete(TABLE_POST_LIKES, "post_id = ? AND user_id = ?", arrayOf(postId.toString(), userId.toString())) > 0
        } else {
            db.insert(TABLE_POST_LIKES, null, ContentValues().apply { put("post_id", postId); put("user_id", userId) }) != -1L
        }
    }

    fun addComment(userId: Int, postId: Int, text: String): Boolean {
        val date = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
        val v = ContentValues().apply { put("post_id", postId); put("user_id", userId); put("comment_text", text); put("comment_date", date) }
        return writableDatabase.insert(TABLE_COMMENTS, null, v) != -1L
    }

    fun getCommentsForPost(postId: Int): List<com.example.fitbody.model.Comment> {
        val list = mutableListOf<com.example.fitbody.model.Comment>()
        val sql = "SELECT c.*, u.username, u.avatar FROM $TABLE_COMMENTS c JOIN $TABLE_USERS u ON c.user_id = u.id WHERE c.post_id = ? ORDER BY c.id ASC"
        val c = readableDatabase.rawQuery(sql, arrayOf(postId.toString()))
        if (c.moveToFirst()) {
            do {
                val id = c.getInt(0)
                val pId = c.getInt(1)
                val uId = c.getInt(2)
                val text = c.getString(3) ?: ""
                val date = c.getString(4) ?: ""
                val username = c.getString(5) ?: "User"
                val avatar = c.getString(6)
                list.add(com.example.fitbody.model.Comment(id, pId, uId, username, avatar, text, date))
            } while (c.moveToNext())
        }
        c.close(); return list
    }
}
