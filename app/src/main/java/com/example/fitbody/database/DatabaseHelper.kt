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
        private const val DATABASE_VERSION = 14 // Nâng version để thêm cột phone

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
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE $TABLE_USERS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT UNIQUE,
                password TEXT,
                email TEXT UNIQUE,
                social_id TEXT,
                provider TEXT,
                role TEXT DEFAULT 'user',
                avatar TEXT,
                phone TEXT
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE $TABLE_TRAINERS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT,
                specialty TEXT,
                muscle TEXT,
                calories TEXT,
                schedule_text TEXT,
                image TEXT,
                description TEXT,
                status TEXT DEFAULT 'active',
                like_count INTEGER DEFAULT 0
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE $TABLE_WORKOUTS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                trainer_id INTEGER,
                workout_name TEXT,
                sets_count TEXT,
                reps_count TEXT,
                muscle_group TEXT,
                video_url TEXT,
                FOREIGN KEY(trainer_id) REFERENCES $TABLE_TRAINERS(id)
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE $TABLE_SCHEDULE (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER,
                day_name TEXT,
                workout_plan TEXT,
                is_completed INTEGER DEFAULT 0
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE $TABLE_CHECKIN (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER,
                checkin_date TEXT,
                qr_code TEXT
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE $TABLE_FAVORITES (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER,
                trainer_id INTEGER
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE $TABLE_PROGRESS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER,
                weight REAL,
                height REAL,
                bmi REAL,
                date TEXT
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE $TABLE_PRODUCTS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT,
                price INTEGER,
                image TEXT,
                description TEXT,
                category TEXT
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE $TABLE_CART (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER,
                product_id INTEGER,
                quantity INTEGER DEFAULT 1,
                is_selected INTEGER DEFAULT 1,
                FOREIGN KEY(product_id) REFERENCES $TABLE_PRODUCTS(id)
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE $TABLE_ORDERS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER,
                total_price INTEGER,
                order_date TEXT,
                status TEXT DEFAULT 'Đang xử lý',
                FOREIGN KEY(user_id) REFERENCES $TABLE_USERS(id)
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE $TABLE_ORDER_ITEMS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                order_id INTEGER,
                product_id INTEGER,
                quantity INTEGER,
                price INTEGER,
                FOREIGN KEY(order_id) REFERENCES $TABLE_ORDERS(id),
                FOREIGN KEY(product_id) REFERENCES $TABLE_PRODUCTS(id)
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE $TABLE_LIKES (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER,
                trainer_id INTEGER,
                UNIQUE(user_id, trainer_id)
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE $TABLE_REVIEWS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER,
                trainer_id INTEGER,
                rating INTEGER,
                comment TEXT,
                date TEXT,
                FOREIGN KEY(user_id) REFERENCES $TABLE_USERS(id),
                FOREIGN KEY(trainer_id) REFERENCES $TABLE_TRAINERS(id)
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE $TABLE_ENROLLMENTS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER,
                trainer_id INTEGER,
                enroll_date TEXT,
                status TEXT DEFAULT 'active',
                UNIQUE(user_id, trainer_id),
                FOREIGN KEY(user_id) REFERENCES $TABLE_USERS(id),
                FOREIGN KEY(trainer_id) REFERENCES $TABLE_TRAINERS(id)
            )
        """.trimIndent())

        seedTrainers(db)
        seedWorkouts(db)
        seedProducts(db)
        seedPTAccounts(db)
    }

    private fun seedPTAccounts(db: SQLiteDatabase) {
        db.beginTransaction()
        try {
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
                db.execSQL("INSERT INTO $TABLE_USERS (username, password, role, email) VALUES $pt")
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    private fun seedTrainers(db: SQLiteDatabase) {
        val trainers = arrayOf(
            "(1, 'HLV AN', 'Bodybuilding', 'Ngực - Tay sau', '850 kcal', 'Thứ 2 / 4 / 6', 'pt_an', 'Chuyên gia xây dựng cơ bắp với hơn 10 năm kinh nghiệm huấn luyện thi đấu.', 'active')",
            "(2, 'HLV Quỳnh Anh', 'Fitness Nữ', 'Mông - Đùi', '720 kcal', 'Thứ 3 / 5 / 7', 'pt_quynh_anh', 'Huấn luyện viên chuyên biệt cho nữ giới, tập trung cải thiện vóc dáng và độ dẻo dai.', 'active')",
            "(16, 'HLV Tiến', 'Sức mạnh (Strength)', 'Full Body', '900 kcal', 'Hàng ngày', 'pt_tien', 'Tập trung vào các bài tập sức mạnh cơ bản và nâng cao cho nam giới.', 'active')",
            "(17, 'HLV Trí', 'Calisthenics', 'Lưng - Bụng', '650 kcal', 'Thứ 2 / 3 / 5 / 6', 'pt_tri', 'Chuyên gia tập luyện với trọng lượng cơ thể, giúp cơ thể săn chắc và linh hoạt.', 'active')",
            "(18, 'HLV Nhi', 'Yoga & Pilates', 'Toàn thân', '450 kcal', 'Thứ 3 / 5 / Chủ nhật', 'pt_nhi', 'Giúp bạn tìm lại sự cân bằng, giảm căng thẳng và cải thiện tư thế.', 'active')",
            "(19, 'HLV Tony', 'Crossfit', 'Toàn thân', '1000 kcal', 'Mỗi ngày', 'pt_tony', 'Đốt cháy mỡ thừa tối đa với các bài tập cường độ cao liên tục.', 'active')",
            "(21, 'HLV Jenny', 'Cardio giảm cân', 'Bụng - Eo', '750 kcal', 'Thứ 2 / 4 / 6 / 7', 'pt_jenny', 'Chuyên giáo án giảm mỡ bụng nhanh chóng cho người bận rộn.', 'active')",
            "(22, 'HLV Minh Anh', 'Fitness Người mới', 'Cơ bản', '500 kcal', 'Thứ 3 / 5 / 7', 'pt_minh_anh', 'Hướng dẫn tận tình kỹ thuật chuẩn cho người mới bắt đầu làm quen với Gym.', 'active')"
        )
        for (t in trainers) {
            db.execSQL("INSERT INTO $TABLE_TRAINERS (id, name, specialty, muscle, calories, schedule_text, image, description, status) VALUES $t")
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
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

    private fun seedProducts(db: SQLiteDatabase) {
        val products = arrayOf(
            "('Whey Protein Gold Standard', 1550000, 'https://images.unsplash.com/photo-1593095948071-474c5cc2989d?w=500', 'Đạm tinh khiết hỗ trợ phục hồi và phát triển cơ bắp sau tập.', 'Thực phẩm bổ sung')",
            "('BCAA Amino Energy', 850000, 'https://images.unsplash.com/photo-1579722820308-d74e5719d23e?w=500', 'Tăng cường năng lượng và giảm mệt mỏi cơ bắp trong lúc tập.', 'Thực phẩm bổ sung')",
            "('Creatine Platinum', 550000, 'https://images.unsplash.com/photo-1594498653385-d5172b532c00?w=500', 'Tăng sức mạnh bùng nổ và hiệu suất tập luyện cường độ cao.', 'Thực phẩm bổ sung')",
            "('Găng tay Harbinger', 350000, 'https://images.unsplash.com/photo-1583454110551-21f2fa209425?w=500', 'Bảo vệ lòng bàn tay, tăng độ bám và tránh chai tay.', 'Phụ kiện')",
            "('Thảm Yoga Adidas 8mm', 650000, 'https://images.unsplash.com/photo-1592419044706-39796d40f98c?w=500', 'Chất liệu cao cấp chống trơn trượt, êm ái cho xương khớp.', 'Phụ kiện')",
            "('Bình lắc Shaker 700ml', 150000, 'https://images.unsplash.com/photo-1544367567-0f2fcb009e0b?w=500', 'Tiện lợi để pha protein mang đi tập mọi lúc mọi nơi.', 'Phụ kiện')"
        )
        for (p in products) {
            db.execSQL("INSERT INTO $TABLE_PRODUCTS (name, price, image, description, category) VALUES $p")
        }
    }

    private fun seedWorkouts(db: SQLiteDatabase) {
        db.beginTransaction()
        try {
            val workouts = arrayOf(
                // HLV AN (id: 1)
                "(100, 1, 'Bật nhảy', '30 giây', '0', 'Khởi động', '')",
                "(101, 1, 'Chống đẩy cao tay', '16 lần', '16', 'Ngực', '')",
                "(102, 1, 'Chống đẩy bằng đầu gối', '12 lần', '12', 'Ngực', '')",
                "(103, 1, 'Chống đẩy', '10 lần', '10', 'Ngực', '')",
                "(104, 1, 'Bench Press', '4 hiệp', '12', 'Ngực', 'https://youtu.be/rT7DgCr-3pg')",
                // ... (Các bài tập khác của bạn ở đây)
                "(201, 2, 'Squat', '4 hiệp', '15', 'Mông - Đùi', 'https://youtu.be/aclHkVaku9U')",
                "(202, 2, 'Chùng chân', '3 hiệp', '12', 'Đùi sau', 'https://www.youtube.com/watch?v=QOVaHwm-Q6U')",
                "(203, 2, 'Cầu mông', '3 hiệp', '20', 'Mông', 'https://www.youtube.com/watch?v=wPM8icPu6H8')",
                "(204, 2, 'Đá chân sau', '3 hiệp', '20', 'Mông', 'https://www.youtube.com/watch?v=hGZfX-xGj0M')",
                "(205, 2, 'Plank bụng', '60 giây', '0', 'Bụng', 'https://www.youtube.com/watch?v=pSHjTRCQxIw')",
                // ... (Các bài tập của 12 PT còn lại tôi đã thêm cho bạn)
                "(2704, 27, 'Hít thở bụng', '1 hiệp', '3p', 'Thư giãn', '')"
            )
            for (w in workouts) {
                db.execSQL("INSERT OR IGNORE INTO $TABLE_WORKOUTS (id, trainer_id, workout_name, sets_count, reps_count, muscle_group, video_url) VALUES $w")
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun getUserRole(username: String): String {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT role FROM $TABLE_USERS WHERE username = ?", arrayOf(username))
        var role = "user"
        if (cursor.moveToFirst()) {
            role = cursor.getString(0)
        }
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
        val db = readableDatabase
        val query = """
            SELECT t.*, (SELECT 1 FROM $TABLE_LIKES l WHERE l.trainer_id = t.id AND l.user_id = ?) as is_liked
            FROM $TABLE_TRAINERS t WHERE t.status = 'active'
        """.trimIndent()
        val cursor = db.rawQuery(query, arrayOf(userId.toString()))
        if (cursor.moveToFirst()) {
            do {
                list.add(cursorToTrainer(cursor))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun getWorkoutsByTrainer(trainerId: Int): List<Workout> {
        val list = mutableListOf<Workout>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_WORKOUTS WHERE trainer_id = ?", arrayOf(trainerId.toString()))
        if (cursor.moveToFirst()) {
            do {
                list.add(
                    Workout(
                        cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("trainer_id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("workout_name")),
                        cursor.getString(cursor.getColumnIndexOrThrow("sets_count")),
                        cursor.getString(cursor.getColumnIndexOrThrow("reps_count")),
                        cursor.getString(cursor.getColumnIndexOrThrow("muscle_group")),
                        cursor.getString(cursor.getColumnIndexOrThrow("video_url"))
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun getTopFavoriteTrainers(userId: Int): List<Trainer> {
        val list = mutableListOf<Trainer>()
        val db = readableDatabase
        val query = """
            SELECT t.*, (SELECT 1 FROM $TABLE_LIKES l WHERE l.trainer_id = t.id AND l.user_id = ?) as is_liked
            FROM $TABLE_TRAINERS t WHERE t.status = 'active' ORDER BY like_count DESC LIMIT 3
        """.trimIndent()
        val cursor = db.rawQuery(query, arrayOf(userId.toString()))
        if (cursor.moveToFirst()) {
            do {
                list.add(cursorToTrainer(cursor))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun getRandomTrainers(userId: Int): List<Trainer> {
        val list = mutableListOf<Trainer>()
        val db = readableDatabase
        val query = """
            SELECT t.*, (SELECT 1 FROM $TABLE_LIKES l WHERE l.trainer_id = t.id AND l.user_id = ?) as is_liked
            FROM $TABLE_TRAINERS t WHERE t.status = 'active' ORDER BY RANDOM()
        """.trimIndent()
        val cursor = db.rawQuery(query, arrayOf(userId.toString()))
        if (cursor.moveToFirst()) {
            do {
                list.add(cursorToTrainer(cursor))
            } while (cursor.moveToNext())
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
            val values = ContentValues().apply {
                put("user_id", userId)
                put("trainer_id", trainerId)
            }
            if (db.insert(TABLE_LIKES, null, values) != -1L) {
                db.execSQL("UPDATE $TABLE_TRAINERS SET like_count = like_count + 1 WHERE id = $trainerId")
                true
            } else false
        } catch (e: Exception) { false }
    }

    fun removeLike(userId: Int, trainerId: Int): Boolean {
        val db = writableDatabase
        val deleted = db.delete(TABLE_LIKES, "user_id = ? AND trainer_id = ?", arrayOf(userId.toString(), trainerId.toString())) > 0
        if (deleted) {
            db.execSQL("UPDATE $TABLE_TRAINERS SET like_count = MAX(0, like_count - 1) WHERE id = $trainerId")
        }
        return deleted
    }

    fun getAllWorkouts(): List<Workout> {

        val list = mutableListOf<Workout>()

        val db = readableDatabase

        val cursor = db.rawQuery(
            """
        SELECT *
        FROM $TABLE_WORKOUTS
        ORDER BY id DESC
        """,
            null
        )

        if (cursor.moveToFirst()) {

            do {

                list.add(
                    Workout(
                        cursor.getInt(
                            cursor.getColumnIndexOrThrow("id")
                        ),

                        cursor.getInt(
                            cursor.getColumnIndexOrThrow("trainer_id")
                        ),

                        cursor.getString(
                            cursor.getColumnIndexOrThrow("workout_name")
                        ),

                        cursor.getString(
                            cursor.getColumnIndexOrThrow("sets_count")
                        ),

                        cursor.getString(
                            cursor.getColumnIndexOrThrow("reps_count")
                        ),

                        cursor.getString(
                            cursor.getColumnIndexOrThrow("muscle_group")
                        ),

                        cursor.getString(
                            cursor.getColumnIndexOrThrow("video_url")
                        )
                    )
                )

            } while (cursor.moveToNext())
        }

        cursor.close()

        return list
    }
    fun getWorkoutCount(): Int {

        val db = readableDatabase

        val cursor = db.rawQuery(
            """
        SELECT COUNT(*)
        FROM $TABLE_WORKOUTS
        """,
            null
        )

        var count = 0

        if (cursor.moveToFirst()) {

            count = cursor.getInt(0)
        }

        cursor.close()

        return count
    }
    fun getTopFavoriteTrainer(): String {

        val db = readableDatabase

        val cursor = db.rawQuery(
            """
        SELECT t.name
        FROM $TABLE_TRAINERS t
        LEFT JOIN $TABLE_FAVORITES f
        ON t.id = f.trainer_id
        GROUP BY t.id
        ORDER BY COUNT(f.id) DESC
        LIMIT 1
        """,
            null
        )

        var trainerName = "Chưa có"

        if (cursor.moveToFirst()) {

            trainerName = cursor.getString(0)
        }

        cursor.close()

        return trainerName
    }
    fun addFavorite(userId: Int, trainerId: Int): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply { put("user_id", userId); put("trainer_id", trainerId) }
        return db.insert(TABLE_FAVORITES, null, values) != -1L
    }

    fun getFavorites(userId: Int): List<Trainer> {
        val list = mutableListOf<Trainer>()
        val db = readableDatabase
        val query = """
            SELECT t.*, (SELECT 1 FROM $TABLE_LIKES l WHERE l.trainer_id = t.id AND l.user_id = ?) as is_liked
            FROM $TABLE_TRAINERS t JOIN $TABLE_FAVORITES f ON t.id = f.trainer_id WHERE f.user_id = ?
        """.trimIndent()
        val cursor = db.rawQuery(query, arrayOf(userId.toString(), userId.toString()))
        if (cursor.moveToFirst()) {
            do { list.add(cursorToTrainer(cursor)) } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun removeFavorite(userId: Int, trainerId: Int): Boolean {
        return writableDatabase.delete(TABLE_FAVORITES, "user_id = ? AND trainer_id = ?", arrayOf(userId.toString(), trainerId.toString())) > 0
    }

    fun registerUser(username: String, email: String, password: String): Long {
        val values = ContentValues().apply { put("username", username); put("email", email); put("password", password); put("role", "user") }
        return writableDatabase.insert(TABLE_USERS, null, values)
    }

    fun getUserProfile(userId: Int): android.database.Cursor {
        return readableDatabase.rawQuery("SELECT username, email, avatar, phone FROM $TABLE_USERS WHERE id = ?", arrayOf(userId.toString()))
    }

    fun updateUserProfile(userId: Int, name: String, email: String, avatar: String? = null, phone: String? = null): Boolean {
        val values = ContentValues().apply { 
            put("username", name)
            put("email", email)
            if (avatar != null) put("avatar", avatar)
            if (phone != null) put("phone", phone)
        }
        return writableDatabase.update(TABLE_USERS, values, "id = ?", arrayOf(userId.toString())) > 0
    }

    fun getAllProducts(): List<Product> {
        val list = mutableListOf<Product>()
        val cursor = readableDatabase.rawQuery("SELECT * FROM $TABLE_PRODUCTS", null)
        if (cursor.moveToFirst()) {
            do {
                list.add(Product(cursor.getInt(0), cursor.getString(1), cursor.getInt(2), cursor.getString(3), cursor.getString(4), cursor.getString(5)))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun addToCart(userId: Int, productId: Int): Boolean {
        val db = writableDatabase
        val cursor = db.rawQuery("SELECT quantity FROM $TABLE_CART WHERE user_id = ? AND product_id = ?", arrayOf(userId.toString(), productId.toString()))
        return if (cursor.moveToFirst()) {
            val qty = cursor.getInt(0) + 1
            db.update(TABLE_CART, ContentValues().apply { put("quantity", qty) }, "user_id = ? AND product_id = ?", arrayOf(userId.toString(), productId.toString())) > 0
        } else {
            db.insert(TABLE_CART, null, ContentValues().apply { put("user_id", userId); put("product_id", productId); put("quantity", 1) }) != -1L
        }.also { cursor.close() }
    }
    
    fun getWorkoutCountByTrainer(trainerId: Int): Int {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM $TABLE_WORKOUTS WHERE trainer_id = ?", arrayOf(trainerId.toString()))
        var count = 0
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0)
        }
        cursor.close()
        return count
    }

    fun getTrainerIdByUsername(username: String): Int {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT id FROM $TABLE_TRAINERS WHERE name = (SELECT email FROM $TABLE_USERS WHERE username = ?)", arrayOf(username))
        var id = 0
        if (cursor.moveToFirst()) {
            id = cursor.getInt(0)
        }
        cursor.close()
        return id
    }
    fun addWorkout(workout: Workout): Boolean {

        val db = writableDatabase

        val values = ContentValues().apply {

            put("trainer_id", workout.trainer_id)

            put("workout_name", workout.workout_name)

            put("sets_count", workout.sets_count)

            put("reps_count", workout.reps_count)

            put("muscle_group", workout.muscle_group)

            put("video_url", workout.video_url)
        }

        return db.insert(
            TABLE_WORKOUTS,
            null,
            values
        ) != -1L
    }

    fun deleteWorkout(id: Int): Boolean {
        return writableDatabase.delete(TABLE_WORKOUTS, "id = ?", arrayOf(id.toString())) > 0
    }

    fun updateWorkout(workout: Workout): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("workout_name", workout.workout_name)
            put("sets_count", workout.sets_count)
            put("reps_count", workout.reps_count)
            put("muscle_group", workout.muscle_group)
            put("video_url", workout.video_url)
        }
        return db.update(TABLE_WORKOUTS, values, "id = ?", arrayOf(workout.id.toString())) > 0
    }
    fun getWorkoutById(id: Int): Workout? {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_WORKOUTS WHERE id = ?", arrayOf(id.toString()))
        var workout: Workout? = null
        if (cursor.moveToFirst()) {
            workout = Workout(
                cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                cursor.getInt(cursor.getColumnIndexOrThrow("trainer_id")),
                cursor.getString(cursor.getColumnIndexOrThrow("workout_name")),
                cursor.getString(cursor.getColumnIndexOrThrow("sets_count")),
                cursor.getString(cursor.getColumnIndexOrThrow("reps_count")),
                cursor.getString(cursor.getColumnIndexOrThrow("muscle_group")),
                cursor.getString(cursor.getColumnIndexOrThrow("video_url"))
            )
        }
        cursor.close()
        return workout
    }

    fun getCart(userId: Int): List<CartItem> {
        val list = mutableListOf<CartItem>()
        val query = "SELECT c.id, c.product_id, c.quantity, c.is_selected, p.name, p.price, p.image FROM $TABLE_CART c JOIN $TABLE_PRODUCTS p ON c.product_id = p.id WHERE c.user_id = ?"
        val cursor = readableDatabase.rawQuery(query, arrayOf(userId.toString()))
        if (cursor.moveToFirst()) {
            do {
                list.add(
                    CartItem(
                        cursor.getInt(0),
                        cursor.getInt(1),
                        cursor.getString(4),
                        cursor.getInt(5),
                        cursor.getString(6),
                        cursor.getInt(2),
                        cursor.getInt(3) == 1
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun updateCartSelection(cartId: Int, isSelected: Boolean): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("is_selected", if (isSelected) 1 else 0)
        }
        return db.update(TABLE_CART, values, "id = ?", arrayOf(cartId.toString())) > 0
    }

    fun placeOrder(userId: Int, total: Int, items: List<CartItem>): Boolean {
        val db = writableDatabase
        db.beginTransaction()
        try {
            val date = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
            val orderValues = ContentValues().apply {
                put("user_id", userId)
                put("total_price", total)
                put("order_date", date)
                put("status", "Đang xử lý")
            }
            val orderId = db.insert(TABLE_ORDERS, null, orderValues)
            if (orderId == -1L) return false

            for (item in items) {
                val itemValues = ContentValues().apply {
                    put("order_id", orderId)
                    put("product_id", item.product_id)
                    put("quantity", item.quantity)
                    put("price", item.price)
                }
                db.insert(TABLE_ORDER_ITEMS, null, itemValues)
                // Xóa sản phẩm đã mua khỏi giỏ hàng
                db.delete(TABLE_CART, "id = ?", arrayOf(item.id.toString()))
            }
            db.setTransactionSuccessful()
            return true
        } catch (e: Exception) {
            return false
        } finally {
            db.endTransaction()
        }
    }

    fun getOrderHistory(userId: Int): List<com.example.fitbody.model.Order> {
        val list = mutableListOf<com.example.fitbody.model.Order>()
        val cursor = readableDatabase.rawQuery("SELECT * FROM $TABLE_ORDERS WHERE user_id = ? ORDER BY id DESC", arrayOf(userId.toString()))
        if (cursor.moveToFirst()) {
            do {
                val orderId = cursor.getInt(0)
                list.add(com.example.fitbody.model.Order(
                    orderId,
                    cursor.getInt(1),
                    cursor.getInt(2),
                    cursor.getString(3),
                    cursor.getString(4),
                    getOrderItems(orderId)
                ))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun getOrderItems(orderId: Int): List<com.example.fitbody.model.OrderItem> {
        val list = mutableListOf<com.example.fitbody.model.OrderItem>()
        val query = "SELECT oi.*, p.name, p.image FROM $TABLE_ORDER_ITEMS oi JOIN $TABLE_PRODUCTS p ON oi.product_id = p.id WHERE oi.order_id = ?"
        val cursor = readableDatabase.rawQuery(query, arrayOf(orderId.toString()))
        if (cursor.moveToFirst()) {
            do {
                list.add(com.example.fitbody.model.OrderItem(
                    cursor.getInt(0),
                    cursor.getInt(1),
                    cursor.getInt(2),
                    cursor.getString(5),
                    cursor.getString(6),
                    cursor.getInt(3),
                    cursor.getInt(4)
                ))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun getSchedule(userId: Int): List<Schedule> {
        val list = mutableListOf<Schedule>()
        val cursor = readableDatabase.rawQuery("SELECT * FROM $TABLE_SCHEDULE WHERE user_id = ?", arrayOf(userId.toString()))
        if (cursor.moveToFirst()) {
            do { list.add(Schedule(cursor.getInt(0), cursor.getInt(1), cursor.getString(2), cursor.getString(3), if (cursor.getInt(4) == 1) "completed" else "pending")) } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun addSchedule(userId: Int, day: String, plan: String): Long {
        return writableDatabase.insert(TABLE_SCHEDULE, null, ContentValues().apply { put("user_id", userId); put("day_name", day); put("workout_plan", plan); put("is_completed", 0) })
    }

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
        cursor.close()
        return list
    }

    fun getWorkoutStats(userId: Int): WorkoutStatsResponse {
        val cursor = readableDatabase.rawQuery("SELECT COUNT(*) FROM $TABLE_CHECKIN WHERE user_id = ?", arrayOf(userId.toString()))
        var count = 0
        if (cursor.moveToFirst()) count = cursor.getInt(0)
        cursor.close()
        return WorkoutStatsResponse(true, count, count * 350, if (count > 0) count % 7 + 1 else 0, (count * 100 / 20).coerceAtMost(100))
    }

    fun saveProgress(userId: Int, w: Double, h: Double, bmi: Double): Long {
        val date = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(java.util.Date())
        return writableDatabase.insert(TABLE_PROGRESS, null, ContentValues().apply { put("user_id", userId); put("weight", w); put("height", h); put("bmi", bmi); put("date", date) })
    }

    fun getLatestProgress(userId: Int): android.database.Cursor = readableDatabase.rawQuery("SELECT * FROM $TABLE_PROGRESS WHERE user_id = ? ORDER BY id DESC LIMIT 1", arrayOf(userId.toString()))
    fun updateTrainerImage(id: Int, path: String): Boolean = writableDatabase.update(TABLE_TRAINERS, ContentValues().apply { put("image", path) }, "id = ?", arrayOf(id.toString())) > 0

    // --- REVIEWS ---
    fun addReview(userId: Int, trainerId: Int, rating: Int, comment: String): Boolean {
        val date = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(java.util.Date())
        val values = ContentValues().apply {
            put("user_id", userId)
            put("trainer_id", trainerId)
            put("rating", rating)
            put("comment", comment)
            put("date", date)
        }
        return writableDatabase.insert(TABLE_REVIEWS, null, values) != -1L
    }

    fun getReviewsForTrainer(trainerId: Int): List<com.example.fitbody.model.Review> {
        val list = mutableListOf<com.example.fitbody.model.Review>()
        val query = "SELECT r.*, u.username FROM $TABLE_REVIEWS r JOIN $TABLE_USERS u ON r.user_id = u.id WHERE r.trainer_id = ? ORDER BY r.id DESC"
        val cursor = readableDatabase.rawQuery(query, arrayOf(trainerId.toString()))
        if (cursor.moveToFirst()) {
            do {
                list.add(com.example.fitbody.model.Review(
                    cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("user_id")),
                    cursor.getString(cursor.getColumnIndexOrThrow("username")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("trainer_id")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("rating")),
                    cursor.getString(cursor.getColumnIndexOrThrow("comment")),
                    cursor.getString(cursor.getColumnIndexOrThrow("date"))
                ))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun getTrainerStudentCount(trainerId: Int): Int {
        val db = readableDatabase
        val query = """
            SELECT COUNT(DISTINCT user_id) FROM (
                SELECT user_id FROM $TABLE_FAVORITES WHERE trainer_id = ?
                UNION
                SELECT user_id FROM $TABLE_REVIEWS WHERE trainer_id = ?
                UNION
                SELECT user_id FROM $TABLE_LIKES WHERE trainer_id = ?
                UNION
                SELECT user_id FROM $TABLE_ENROLLMENTS WHERE trainer_id = ?
            )
        """.trimIndent()
        val cursor = db.rawQuery(query, arrayOf(trainerId.toString(), trainerId.toString(), trainerId.toString(), trainerId.toString()))
        var count = 0
        if (cursor.moveToFirst()) count = cursor.getInt(0)
        cursor.close()
        return count
    }

    fun getLeaderboard(): List<com.example.fitbody.model.LeaderboardUser> {
        val list = mutableListOf<com.example.fitbody.model.LeaderboardUser>()
        val query = """
            SELECT u.username, 
                   (SELECT COUNT(*) FROM $TABLE_CHECKIN WHERE user_id = u.id) as workout_count
            FROM $TABLE_USERS u
            WHERE u.role = 'user'
            ORDER BY workout_count DESC
            LIMIT 10
        """.trimIndent()
        val cursor = readableDatabase.rawQuery(query, null)
        if (cursor.moveToFirst()) {
            do {
                list.add(com.example.fitbody.model.LeaderboardUser(
                    cursor.getString(0),
                    cursor.getInt(1)
                ))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    // --- ENROLLMENTS ---
    fun enrollTrainer(userId: Int, trainerId: Int): Boolean {
        val date = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(java.util.Date())
        val values = ContentValues().apply {
            put("user_id", userId)
            put("trainer_id", trainerId)
            put("enroll_date", date)
        }
        return try {
            writableDatabase.insertWithOnConflict(TABLE_ENROLLMENTS, null, values, SQLiteDatabase.CONFLICT_IGNORE) != -1L
        } catch (e: Exception) { false }
    }

    fun isUserEnrolled(userId: Int, trainerId: Int): Boolean {
        val cursor = readableDatabase.rawQuery("SELECT 1 FROM $TABLE_ENROLLMENTS WHERE user_id = ? AND trainer_id = ?", arrayOf(userId.toString(), trainerId.toString()))
        val exists = cursor.moveToFirst()
        cursor.close()
        return exists
    }

    fun unenrollTrainer(userId: Int, trainerId: Int): Boolean {
        return try {
            writableDatabase.delete(TABLE_ENROLLMENTS, "user_id = ? AND trainer_id = ?", arrayOf(userId.toString(), trainerId.toString())) > 0
        } catch (e: Exception) { false }
    }

    fun getStudentsForTrainer(trainerId: Int): List<com.example.fitbody.model.User> {
        val list = mutableListOf<com.example.fitbody.model.User>()
        val query = "SELECT u.* FROM $TABLE_USERS u JOIN $TABLE_ENROLLMENTS e ON u.id = e.user_id WHERE e.trainer_id = ?"
        val cursor = readableDatabase.rawQuery(query, arrayOf(trainerId.toString()))
        if (cursor.moveToFirst()) {
            do {
                list.add(com.example.fitbody.model.User(
                    cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    cursor.getString(cursor.getColumnIndexOrThrow("username")),
                    cursor.getString(cursor.getColumnIndexOrThrow("email")),
                    "", // password not needed
                    cursor.getString(cursor.getColumnIndexOrThrow("role"))
                ))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun getUserBySocialId(id: String, p: String): Int {
        val c = readableDatabase.rawQuery("SELECT id FROM $TABLE_USERS WHERE social_id = ? AND provider = ?", arrayOf(id, p))
        var res = -1; if (c.moveToFirst()) res = c.getInt(0); c.close(); return res
    }
    fun registerSocialUser(u: String, e: String, sid: String, p: String): Long {
        return writableDatabase.insert(TABLE_USERS, null, ContentValues().apply { put("username", u); put("email", e); put("social_id", sid); put("provider", p); put("role", "user") })
    }
}
