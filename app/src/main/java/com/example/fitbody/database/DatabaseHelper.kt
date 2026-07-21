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
import java.util.*

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "fitbody.db"
        private const val DATABASE_VERSION = 22

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

    private fun seedWorkouts(db: SQLiteDatabase) {
        val workouts = arrayOf(
            // HLV AN (ID: 1) - Ngực & Tay sau
            "(100, 1, 'Bật nhảy', '3 hiệp', '15 lần', 'Khởi động', '')",
            "(101, 1, 'Chống đẩy cao tay', '3 hiệp', '12 lần', 'Ngực', '')",
            "(102, 1, 'Chống đẩy bằng đầu gối', '3 hiệp', '10 lần', 'Ngực', '')",
            "(103, 1, 'Chống đẩy', '4 hiệp', '12 lần', 'Ngực', '')",
            "(104, 1, 'Bench Press', '4 hiệp', '10 lần', 'Ngực', 'https://youtu.be/rT7DgCr-3pg')",
            "(105, 1, 'Banh ngực tạ đôi', '3 hiệp', '12 lần', 'Ngực', '')",

            // HLV Quỳnh Anh (ID: 2) - Mông & Đùi
            "(201, 2, 'Squat', '4 hiệp', '15 lần', 'Mông - Đùi', 'https://youtu.be/aclHkVaku9U')",
            "(202, 2, 'Chùng chân', '3 hiệp', '12 lần', 'Đùi sau', 'https://www.youtube.com/watch?v=QOVaHwm-Q6U')",
            "(203, 2, 'Plank bụng', '3 hiệp', '60 giây', 'Bụng', 'https://www.youtube.com/watch?v=pSHjTRCQxIw')",
            "(204, 2, 'Cầu mông', '4 hiệp', '20 lần', 'Mông', '')",
            "(205, 2, 'Gánh tạ nặng', '4 hiệp', '8 lần', 'Mông - Đùi', '')",
            "(206, 2, 'Squat một chân', '3 hiệp', '10 lần', 'Mông - Đùi', '')",

            // HLV Tiến (ID: 16) - Sức mạnh
            "(301, 16, 'Deadlift cơ bản', '4 hiệp', '8 lần', 'Toàn thân', 'https://youtu.be/op9kVnSso6Q')",
            "(302, 16, 'Hít xà đơn', '3 hiệp', '10 lần', 'Lưng', '')",
            "(303, 16, 'Kéo tạ đơn', '4 hiệp', '10 lần', 'Lưng', '')",
            "(304, 16, 'Kéo xà rộng tay', '3 hiệp', '8 lần', 'Lưng', '')",

            // HLV Trí (ID: 17) - Calisthenics
            "(401, 17, 'Chống xà kép', '3 hiệp', '12 lần', 'Ngực - Tay sau', '')",
            "(402, 17, 'Hít đất vỗ tay', '3 hiệp', '10 lần', 'Ngực', '')",
            "(403, 17, 'Hít đất kim cương', '3 hiệp', '12 lần', 'Tay sau', '')",
            "(404, 17, 'Trồng chuối dựa tường', '3 hiệp', '30 giây', 'Vai', '')",

            // HLV Nhi (ID: 18) - Yoga
            "(501, 18, 'Tư thế cái cây', '3 hiệp', '45 giây', 'Thăng bằng', '')",
            "(502, 18, 'Tư thế chiến binh', '3 hiệp', '45 giây', 'Toàn thân', '')",
            "(503, 18, 'Tư thế chó úp mặt', '3 hiệp', '60 giây', 'Giãn cơ', '')",
            "(504, 18, 'Tư thế rắn hổ mang', '3 hiệp', '45 giây', 'Lưng', '')",

            // HLV Tony (ID: 19) - HIIT
            "(601, 19, 'Bật nhảy Jack', '4 hiệp', '45 giây', 'Cardio', '')",
            "(602, 19, 'Bật nhảy ngang', '4 hiệp', '30 giây', 'Cardio', '')",
            "(603, 19, 'Leo núi', '4 hiệp', '30 giây', 'Bụng', '')",
            "(604, 19, 'Chạy tại chỗ', '4 hiệp', '60 giây', 'Tim mạch', '')",

            // HLV Jenny (ID: 20) - Pilates
            "(701, 20, 'Gập bụng chữ V', '3 hiệp', '15 lần', 'Bụng', '')",
            "(702, 20, 'Đạp xe trên không', '3 hiệp', '20 lần', 'Bụng', '')",
            "(703, 20, 'Hít thở bụng', '3 hiệp', '2 phút', 'Phục hồi', '')",

            // HLV Minh Anh (ID: 21) - Cardio
            "(801, 21, 'Nhảy dây', '5 hiệp', '2 phút', 'Tim mạch', '')",
            "(802, 21, 'Nhảy cóc', '3 hiệp', '15 lần', 'Đùi', '')",
            "(803, 21, 'Di chuyển bộ chân', '4 hiệp', '1 phút', 'Linh hoạt', '')",

            // HLV Bảo Ngọc (ID: 22) - Khởi động
            "(901, 22, 'Cúi người chạm mũi chân', '2 hiệp', '15 lần', 'Giãn cơ', '')",
            "(902, 22, 'Vặn mình khởi động', '2 hiệp', '20 lần', 'Toàn thân', '')",
            "(903, 22, 'Xoay khớp vai', '2 hiệp', '20 lần', 'Vai', '')",

            // HLV Hoàng Nam (ID: 23) - Thể hình
            "(1001, 23, 'Đẩy ngực trên', '4 hiệp', '12 lần', 'Ngực', '')",
            "(1002, 23, 'Đẩy vai tạ đôi', '4 hiệp', '10 lần', 'Vai', '')",
            "(1003, 23, 'Cuốn tạ tay', '3 hiệp', '12 lần', 'Tay trước', '')",

            // HLV Quốc Huy (ID: 24) - Boxing
            "(1101, 24, 'Đấm móc', '4 hiệp', '30 giây', 'Combat', '')",
            "(1102, 24, 'Đấm vòng', '4 hiệp', '30 giây', 'Combat', '')",
            "(1103, 24, 'Đấm thẳng liên tục', '4 hiệp', '1 phút', 'Combat', '')",
            "(1104, 24, 'Né đòn linh hoạt', '4 hiệp', '1 phút', 'Phản xạ', '')",

            // HLV Kim Chi (ID: 25) - Phục hồi
            "(1201, 25, 'Căng vai', '3 hiệp', '30 giây', 'Giãn cơ', '')",
            "(1202, 25, 'Giãn cơ lưng', '3 hiệp', '30 giây', 'Giãn cơ', '')",
            "(1203, 25, 'Giãn cơ đùi sau', '3 hiệp', '30 giây', 'Giãn cơ', '')",
            "(1204, 25, 'Xoay cổ nhẹ nhàng', '2 hiệp', '1 phút', 'Cổ', '')",

            // HLV Tuấn Kiệt (ID: 26) - Street Workout
            "(1301, 26, 'Gập bụng trên xà', '3 hiệp', '12 lần', 'Bụng', '')",
            "(1302, 26, 'Hít đất vỗ tay', '4 hiệp', '8 lần', 'Ngực', '')",

            // HLV Lan Hương (ID: 27) - Thể dục
            "(1401, 27, 'Nâng mông nhẹ nhàng', '3 hiệp', '15 lần', 'Mông', '')",
            "(1402, 27, 'Nâng chân nằm nghiêng', '3 hiệp', '15 lần', 'Đùi', '')",
            "(1403, 27, 'Đi bộ chậm', '1 hiệp', '10 phút', 'Tim mạch', '')"
        )
        for (w in workouts) {
            db.execSQL("INSERT OR IGNORE INTO $TABLE_WORKOUTS (id, trainer_id, workout_name, sets_count, reps_count, muscle_group, video_url) VALUES $w")
        }
    }

    private fun seedProducts(db: SQLiteDatabase) {
        val products = arrayOf(
            // TRANG 1
            "(1, 'Rule 1 - Pump (30 lần dùng)', 650000, 800000, 'prod_rule1_pump', 'Tăng sức mạnh bùng nổ cho buổi tập.', 'Tăng sức mạnh', 'Còn hàng', 1, 50, 12)",
            "(2, 'Whey Gold Standard 5lbs', 1550000, 1800000, 'prod_whey_gold', 'Đạm tinh khiết giúp phát triển cơ bắp.', 'Protein', 'Còn hàng', 1, 30, 45)",
            "(3, 'Mutant Mass 5lbs', 950000, 1100000, 'prod_mutant_mass', 'Sữa tăng cân cực nhanh cho người gầy.', 'Tăng cân', 'Còn hàng', 0, 40, 8)",
            "(4, 'Kirkland - Vitamin C', 640000, 750000, 'prod_vitamin_c', 'Hỗ trợ hệ miễn dịch và sức khỏe tổng thể.', 'Sức khỏe', 'Còn hàng', 0, 100, 150)",
            "(5, 'BCAA Amino Energy', 850000, 950000, 'prod_bcaa', 'Tăng năng lượng và phục hồi cơ bắp.', 'Phục hồi', 'Còn hàng', 1, 25, 33)",
            "(6, 'Creatine OstroVit', 550000, 650000, 'prod_creatine', 'Tăng sức mạnh và kích thước cơ bắp.', 'Tăng sức mạnh', 'Còn hàng', 0, 60, 21)",
            
            // TRANG 2
            "(7, 'ISO 100 5lbs', 1950000, 2200000, 'prod_iso100', 'Dòng Whey Protein cao cấp nhất hiện nay.', 'Protein', 'Còn hàng', 1, 15, 10)",
            "(8, 'Lipo 6 Black', 750000, 850000, 'prod_lipo6', 'Viên uống hỗ trợ đốt mỡ cực mạnh.', 'Giảm mỡ', 'Còn hàng', 0, 40, 56)",
            "(9, 'Omega 3 Kirkland', 450000, 550000, 'prod_omega3', 'Tốt cho tim mạch và sức khỏe não bộ.', 'Sức khỏe', 'Còn hàng', 0, 80, 200)",
            "(10, 'Pre-workout ABE', 790000, 890000, 'prod_abe', 'Kích thích tập luyện bùng nổ.', 'Tăng sức mạnh', 'Còn hàng', 1, 35, 19)",
            "(11, 'Glucosamine 375 viên', 750000, 850000, 'prod_glucosamine', 'Bảo vệ xương khớp chắc khỏe.', 'Sức khỏe', 'Còn hàng', 0, 50, 42)",
            "(12, 'Sữa tăng cân Serious Mass', 1350000, 1500000, 'prod_serious_mass', 'Tăng cân nhanh cho người khó hấp thụ.', 'Tăng cân', 'Còn hàng', 1, 20, 15)",

            // TRANG 3
            "(13, 'Bình lắc Shaker 700ml', 150000, 200000, 'prod_shaker', 'Tiện lợi để pha Protein mọi lúc.', 'Phụ kiện', 'Còn hàng', 0, 120, 500)",
            "(14, 'Găng tay tập Gym', 250000, 350000, 'prod_gloves', 'Bảo vệ bàn tay khỏi chai sần.', 'Phụ kiện', 'Còn hàng', 0, 45, 89)",
            "(15, 'Thảm tập Yoga', 450000, 600000, 'prod_yoga_mat', 'Chống trượt mồ hôi êm ái.', 'Phụ kiện', 'Còn hàng', 0, 30, 24)",
            "(16, 'Đai lưng tập tạ', 550000, 700000, 'prod_belt', 'Bảo vệ cột sống khi đẩy tạ nặng.', 'Phụ kiện', 'Còn hàng', 0, 25, 13)",
            "(17, 'Dây kéo xà Straps', 120000, 180000, 'prod_straps', 'Hỗ trợ cầm nắm tạ chắc chắn.', 'Phụ kiện', 'Còn hàng', 0, 100, 156)",
            "(18, 'Ống đồng bảo vệ chân', 300000, 450000, 'prod_shin_guards', 'Bảo vệ chân khi tập Kickboxing.', 'Phụ kiện', 'Còn hàng', 0, 40, 7)"
        )
        for (p in products) {
            db.execSQL("INSERT OR IGNORE INTO $TABLE_PRODUCTS (id, name, price, original_price, image, description, category, stock_status, has_gift, stock_quantity, sold_quantity) VALUES $p")
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_TRAINERS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_WORKOUTS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PRODUCTS")
        onCreate(db)
    }

    fun registerUser(username: String, email: String, password: String): Long {
        val v = ContentValues().apply { put("username", username); put("email", email); put("password", password); put("role", "user") }
        return writableDatabase.insert(TABLE_USERS, null, v)
    }

    fun checkUser(username: String, password: String): Int {
        val c = readableDatabase.rawQuery("SELECT id FROM $TABLE_USERS WHERE username = ? AND password = ?", arrayOf(username, password))
        var id = -1; if (c.moveToFirst()) id = c.getInt(0); c.close(); return id
    }

    fun getUserRole(username: String): String {
        val c = readableDatabase.rawQuery("SELECT role FROM $TABLE_USERS WHERE username = ?", arrayOf(username))
        var role = "user"; if (c.moveToFirst()) role = c.getString(0); c.close(); return role
    }

    fun getUserById(id: Int): com.example.fitbody.model.User? {
        val c = readableDatabase.rawQuery("SELECT * FROM $TABLE_USERS WHERE id = ?", arrayOf(id.toString()))
        return if (c.moveToFirst()) {
            val u = com.example.fitbody.model.User(c.getInt(0), c.getString(1), c.getString(3), c.getString(2), c.getString(6), c.getString(7))
            c.close(); u
        } else { c.close(); null }
    }

    fun getUserProfile(userId: Int): android.database.Cursor = readableDatabase.rawQuery("SELECT username, email, avatar, phone, address FROM $TABLE_USERS WHERE id = ?", arrayOf(userId.toString()))

    fun updateUserProfile(id: Int, n: String, e: String, a: String?, p: String?, ad: String?): Boolean {
        val v = ContentValues().apply { put("username", n); put("email", e); if (a != null) put("avatar", a); if (p != null) put("phone", p); if (ad != null) put("address", ad) }
        return writableDatabase.update(TABLE_USERS, v, "id = ?", arrayOf(id.toString())) > 0
    }

    fun updateUserAvatar(id: Int, a: String): Boolean = writableDatabase.update(TABLE_USERS, ContentValues().apply { put("avatar", a) }, "id = ?", arrayOf(id.toString())) > 0

    fun getAllTrainers(userId: Int): List<Trainer> {
        val list = mutableListOf<Trainer>()
        val c = readableDatabase.rawQuery("SELECT t.*, (SELECT 1 FROM $TABLE_LIKES l WHERE l.trainer_id = t.id AND l.user_id = ?) as is_liked FROM $TABLE_TRAINERS t WHERE t.status = 'active'", arrayOf(userId.toString()))
        if (c.moveToFirst()) { do { list.add(cursorToTrainer(c)) } while (c.moveToNext()) }
        c.close(); return list
    }

    private fun cursorToTrainer(c: android.database.Cursor): Trainer {
        val idIdx = c.getColumnIndexOrThrow("id")
        val nameIdx = c.getColumnIndexOrThrow("name")
        val specialtyIdx = c.getColumnIndexOrThrow("specialty")
        val muscleIdx = c.getColumnIndexOrThrow("muscle")
        val caloriesIdx = c.getColumnIndexOrThrow("calories")
        val scheduleIdx = c.getColumnIndexOrThrow("schedule_text")
        val imageIdx = c.getColumnIndexOrThrow("image")
        val descIdx = c.getColumnIndexOrThrow("description")
        val likeCountIdx = c.getColumnIndexOrThrow("like_count")
        val isLikedIdx = c.getColumnIndexOrThrow("is_liked")
        
        return Trainer(
            c.getInt(idIdx),
            c.getString(nameIdx),
            c.getString(specialtyIdx),
            c.getString(muscleIdx),
            c.getString(caloriesIdx),
            c.getString(scheduleIdx),
            c.getString(imageIdx),
            c.getString(descIdx),
            c.getInt(likeCountIdx),
            c.getInt(isLikedIdx) == 1
        )
    }

    fun getWorkoutsByTrainer(id: Int): List<Workout> {
        val list = mutableListOf<Workout>()
        val c = readableDatabase.rawQuery("SELECT * FROM $TABLE_WORKOUTS WHERE trainer_id = ?", arrayOf(id.toString()))
        if (c.moveToFirst()) { do { list.add(Workout(c.getInt(0), c.getInt(1), c.getString(2), c.getString(3), c.getString(4), c.getString(5), c.getString(6))) } while (c.moveToNext()) }
        c.close(); return list
    }

    fun getTrainerIdByUsername(u: String): Int {
        val c = readableDatabase.rawQuery("SELECT id FROM $TABLE_TRAINERS WHERE name = (SELECT email FROM $TABLE_USERS WHERE username = ?)", arrayOf(u))
        var id = 0; if (c.moveToFirst()) id = c.getInt(0); c.close(); return id
    }

    fun getTrainerStudentCount(id: Int): Int {
        val c = readableDatabase.rawQuery("SELECT COUNT(DISTINCT user_id) FROM $TABLE_ENROLLMENTS WHERE trainer_id = ?", arrayOf(id.toString()))
        var count = 0; if (c.moveToFirst()) count = c.getInt(0); c.close(); return count
    }

    fun getStudentsForTrainer(id: Int): List<com.example.fitbody.model.User> {
        val list = mutableListOf<com.example.fitbody.model.User>()
        val c = readableDatabase.rawQuery("SELECT u.* FROM $TABLE_USERS u JOIN $TABLE_ENROLLMENTS e ON u.id = e.user_id WHERE e.trainer_id = ?", arrayOf(id.toString()))
        if (c.moveToFirst()) { do { list.add(com.example.fitbody.model.User(c.getInt(0), c.getString(1), c.getString(3), "", c.getString(6), c.getString(7))) } while (c.moveToNext()) }
        c.close(); return list
    }

    fun getProductsByPage(p: Int, s: Int, cat: String = "Tất cả"): List<Product> {
        val list = mutableListOf<Product>()
        val off = (p - 1) * s
        val sql = if (cat == "Tất cả") "SELECT * FROM $TABLE_PRODUCTS LIMIT ? OFFSET ?" else "SELECT * FROM $TABLE_PRODUCTS WHERE category LIKE ? LIMIT ? OFFSET ?"
        val args = if (cat == "Tất cả") arrayOf(s.toString(), off.toString()) else arrayOf("%$cat%", s.toString(), off.toString())
        val c = readableDatabase.rawQuery(sql, args)
        if (c.moveToFirst()) { do { list.add(Product(c.getInt(0), c.getString(1), c.getInt(2), c.getInt(3), c.getString(4), c.getString(5), c.getString(6), c.getString(7) == "Còn hàng", c.getInt(8) == 1, c.getInt(9), c.getInt(10))) } while (c.moveToNext()) }
        c.close(); return list
    }

    fun getTotalProductCount(cat: String = "Tất cả"): Int {
        val sql = if (cat == "Tất cả") "SELECT COUNT(*) FROM $TABLE_PRODUCTS" else "SELECT COUNT(*) FROM $TABLE_PRODUCTS WHERE category LIKE ?"
        val c = readableDatabase.rawQuery(sql, if (cat == "Tất cả") null else arrayOf("%$cat%"))
        var count = 0; if (c.moveToFirst()) count = c.getInt(0); c.close(); return count
    }

    fun addToCart(uId: Int, pId: Int, q: Int): Boolean {
        val db = writableDatabase
        val c = db.rawQuery("SELECT quantity FROM $TABLE_CART WHERE user_id = ? AND product_id = ?", arrayOf(uId.toString(), pId.toString()))
        return if (c.moveToFirst()) {
            db.update(TABLE_CART, ContentValues().apply { put("quantity", c.getInt(0) + q) }, "user_id = ? AND product_id = ?", arrayOf(uId.toString(), pId.toString())) > 0
        } else {
            db.insert(TABLE_CART, null, ContentValues().apply { put("user_id", uId); put("product_id", pId); put("quantity", q); put("is_selected", 1) }) != -1L
        }.also { c.close() }
    }

    fun getCart(uId: Int): List<CartItem> {
        val list = mutableListOf<CartItem>()
        val c = readableDatabase.rawQuery("SELECT c.id, c.product_id, c.quantity, c.is_selected, p.name, p.price, p.image FROM $TABLE_CART c JOIN $TABLE_PRODUCTS p ON c.product_id = p.id WHERE c.user_id = ?", arrayOf(uId.toString()))
        if (c.moveToFirst()) { do { list.add(CartItem(c.getInt(0), c.getInt(1), c.getString(4), c.getInt(5), c.getString(6), c.getInt(2), c.getInt(3) == 1)) } while (c.moveToNext()) }
        c.close(); return list
    }

    fun updateCartSelection(id: Int, s: Boolean): Boolean = writableDatabase.update(TABLE_CART, ContentValues().apply { put("is_selected", if (s) 1 else 0) }, "id = ?", arrayOf(id.toString())) > 0

    fun placeOrder(uId: Int, t: Int, items: List<CartItem>, pay: String, n: String, ph: String, ad: String): Long {
        val db = writableDatabase; db.beginTransaction()
        try {
            val date = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(java.util.Date())
            val orderId = db.insert(TABLE_ORDERS, null, ContentValues().apply { put("user_id", uId); put("total_price", t); put("order_date", date); put("status", "Đang xử lý"); put("payment_method", pay); put("receiver_name", n); put("receiver_phone", ph); put("receiver_address", ad) })
            if (orderId == -1L) return -1L
            for (item in items) {
                db.insert(TABLE_ORDER_ITEMS, null, ContentValues().apply { put("order_id", orderId); put("product_id", item.productId); put("quantity", item.quantity); put("price", item.price) })
                db.execSQL("UPDATE $TABLE_PRODUCTS SET stock_quantity = MAX(0, stock_quantity - ?), sold_quantity = sold_quantity + ? WHERE id = ?", arrayOf(item.quantity, item.quantity, item.productId))
                db.delete(TABLE_CART, "product_id = ? AND user_id = ?", arrayOf(item.productId.toString(), uId.toString()))
            }
            db.setTransactionSuccessful(); return orderId
        } catch (e: Exception) { return -1L } finally { db.endTransaction() }
    }

    fun getOrderHistory(uId: Int): List<com.example.fitbody.model.Order> {
        val list = mutableListOf<com.example.fitbody.model.Order>()
        val c = readableDatabase.rawQuery("SELECT * FROM $TABLE_ORDERS WHERE user_id = ? ORDER BY id DESC", arrayOf(uId.toString()))
        if (c.moveToFirst()) { do { val id = c.getInt(0); list.add(com.example.fitbody.model.Order(id, c.getInt(1), c.getInt(2), c.getString(3), c.getString(4), c.getString(5), c.getString(6), c.getString(7), c.getString(8), c.getString(9), c.getString(10), getOrderItems(id))) } while (c.moveToNext()) }
        c.close(); return list
    }

    fun getOrderById(id: Int): com.example.fitbody.model.Order? {
        val c = readableDatabase.rawQuery("SELECT * FROM $TABLE_ORDERS WHERE id = ?", arrayOf(id.toString()))
        var o: com.example.fitbody.model.Order? = null
        if (c.moveToFirst()) o = com.example.fitbody.model.Order(c.getInt(0), c.getInt(1), c.getInt(2), c.getString(3), c.getString(4), c.getString(5), c.getString(6), c.getString(7), c.getString(8), c.getString(9), c.getString(10), getOrderItems(id))
        c.close(); return o
    }

    fun getOrderItems(orderId: Int): List<com.example.fitbody.model.OrderItem> {
        val list = mutableListOf<com.example.fitbody.model.OrderItem>()
        val query = "SELECT oi.*, p.name, p.image FROM $TABLE_ORDER_ITEMS oi JOIN $TABLE_PRODUCTS p ON oi.product_id = p.id WHERE oi.order_id = ?"
        val cursor = readableDatabase.rawQuery(query, arrayOf(orderId.toString()))
        if (cursor.moveToFirst()) {
            do {
                list.add(com.example.fitbody.model.OrderItem(
                    cursor.getInt(0), cursor.getInt(1), cursor.getInt(2),
                    cursor.getString(cursor.getColumnIndexOrThrow("name")),
                    cursor.getString(cursor.getColumnIndexOrThrow("image")),
                    cursor.getInt(3), cursor.getInt(4)
                ))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun updateOrderStatus(id: Int, s: String, refundReason: String? = null): Boolean {
        val v = ContentValues().apply {
            put("status", s)
            if (refundReason != null) put("refund_reason", refundReason)
        }
        return writableDatabase.update(TABLE_ORDERS, v, "id = ?", arrayOf(id.toString())) > 0
    }

    fun addFavorite(uId: Int, tId: Int): Boolean = writableDatabase.insert(TABLE_FAVORITES, null, ContentValues().apply { put("user_id", uId); put("trainer_id", tId) }) != -1L
    
    fun getFavorites(uId: Int): List<Trainer> {
        val list = mutableListOf<Trainer>()
        val c = readableDatabase.rawQuery("SELECT t.*, 1 as is_liked FROM $TABLE_TRAINERS t JOIN $TABLE_FAVORITES f ON t.id = f.trainer_id WHERE f.user_id = ?", arrayOf(userIdToString(uId)))
        if (c.moveToFirst()) { do { list.add(cursorToTrainer(c)) } while (c.moveToNext()) }
        c.close(); return list
    }

    private fun userIdToString(id: Int): String = id.toString()

    fun removeFavorite(uId: Int, tId: Int): Boolean = writableDatabase.delete(TABLE_FAVORITES, "user_id = ? AND trainer_id = ?", arrayOf(uId.toString(), tId.toString())) > 0

    fun getLeaderboard(): List<com.example.fitbody.model.LeaderboardUser> {
        val list = mutableListOf<com.example.fitbody.model.LeaderboardUser>()
        val c = readableDatabase.rawQuery("SELECT u.username, (SELECT COUNT(*) FROM $TABLE_CHECKIN WHERE user_id = u.id) as count, u.avatar FROM $TABLE_USERS u WHERE u.role = 'user' ORDER BY count DESC LIMIT 10", null)
        if (c.moveToFirst()) { do { list.add(com.example.fitbody.model.LeaderboardUser(c.getString(0), c.getInt(1), c.getString(2))) } while (c.moveToNext()) }
        c.close(); return list
    }

    fun clearAllData() {
        val db = writableDatabase
        db.delete(TABLE_CART, null, null)
        db.delete(TABLE_ORDERS, null, null)
        db.delete(TABLE_ORDER_ITEMS, null, null)
        db.delete(TABLE_CHECKIN, null, null)
        db.delete(TABLE_PROGRESS, null, null)
        db.delete(TABLE_SCHEDULE, null, null)
        db.delete(TABLE_FAVORITES, null, null)
        db.delete(TABLE_LIKES, null, null)
        db.delete(TABLE_REVIEWS, null, null)
    }
    
    fun syncProductsFromServer(products: List<Product>) {
        val db = writableDatabase; db.beginTransaction()
        try {
            for (p in products) {
                val v = ContentValues().apply { put("name", p.name); put("price", p.price); put("original_price", p.originalPrice); put("image", p.image); put("description", p.description); put("category", p.category) }
                db.update(TABLE_PRODUCTS, v, "id = ?", arrayOf(p.id.toString()))
            }
            db.setTransactionSuccessful()
        } finally { db.endTransaction() }
    }

    fun addLike(uId: Int, tId: Int): Boolean {
        val db = writableDatabase
        return if (db.insert(TABLE_LIKES, null, ContentValues().apply { put("user_id", uId); put("trainer_id", tId) }) != -1L) {
            db.execSQL("UPDATE $TABLE_TRAINERS SET like_count = like_count + 1 WHERE id = ?", arrayOf(tId))
            true
        } else false
    }

    fun removeLike(uId: Int, tId: Int): Boolean {
        val db = writableDatabase
        val deleted = db.delete(TABLE_LIKES, "user_id = ? AND trainer_id = ?", arrayOf(uId.toString(), tId.toString())) > 0
        if (deleted) db.execSQL("UPDATE $TABLE_TRAINERS SET like_count = MAX(0, like_count - 1) WHERE id = ?", arrayOf(tId))
        return deleted
    }

    fun addCheckIn(uId: Int, qr: String): Long = writableDatabase.insert(TABLE_CHECKIN, null, ContentValues().apply { put("user_id", uId); put("qr_code", qr); put("checkin_date", java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(java.util.Date())) })
    
    fun deleteWorkout(id: Int): Boolean = writableDatabase.delete(TABLE_WORKOUTS, "id = ?", arrayOf(id.toString())) > 0

    fun getCheckInHistoryList(uId: Int): List<CheckIn> {
        val list = mutableListOf<CheckIn>()
        val c = readableDatabase.rawQuery("SELECT * FROM $TABLE_CHECKIN WHERE user_id = ? ORDER BY id DESC", arrayOf(uId.toString()))
        if (c.moveToFirst()) { do { list.add(CheckIn(c.getInt(0), c.getInt(1), c.getString(2))) } while (c.moveToNext()) }
        c.close(); return list
    }
    
    fun getUserBySocialId(id: String, p: String): Int {
        val c = readableDatabase.rawQuery("SELECT id FROM $TABLE_USERS WHERE social_id = ? AND provider = ?", arrayOf(id, p))
        var res = -1; if (c.moveToFirst()) res = c.getInt(0); c.close(); return res
    }

    fun registerSocialUser(u: String, e: String, sid: String, p: String): Long = writableDatabase.insert(TABLE_USERS, null, ContentValues().apply { put("username", u); put("email", e); put("social_id", sid); put("provider", p); put("role", "user") })

    fun saveProgress(uId: Int, w: Double, h: Double, bmi: Double): Long = writableDatabase.insert(TABLE_PROGRESS, null, ContentValues().apply { put("user_id", uId); put("weight", w); put("height", h); put("bmi", bmi); put("date", java.text.SimpleDateFormat("dd/MM/yyyy").format(java.util.Date())) })
    fun getLatestProgress(userId: Int): android.database.Cursor = readableDatabase.rawQuery("SELECT * FROM $TABLE_PROGRESS WHERE user_id = ? ORDER BY id DESC LIMIT 1", arrayOf(userId.toString()))

    fun getTopFavoriteTrainer(): String {
        val c = readableDatabase.rawQuery("SELECT name FROM $TABLE_TRAINERS ORDER BY like_count DESC LIMIT 1", null)
        var name = "Chưa có"; if (c.moveToFirst()) name = c.getString(0); c.close(); return name
    }
    fun getTopFavoriteTrainers(userId: Int): List<Trainer> {
        val list = mutableListOf<Trainer>()
        val c = readableDatabase.rawQuery("SELECT t.*, (SELECT 1 FROM $TABLE_LIKES l WHERE l.trainer_id = t.id AND l.user_id = ?) as is_liked FROM $TABLE_TRAINERS t WHERE t.status = 'active' ORDER BY like_count DESC LIMIT 3", arrayOf(userId.toString()))
        if (c.moveToFirst()) { do { list.add(cursorToTrainer(c)) } while (c.moveToNext()) }
        c.close(); return list
    }
    fun getRandomTrainers(userId: Int): List<Trainer> {
        val list = mutableListOf<Trainer>()
        val c = readableDatabase.rawQuery("SELECT t.*, (SELECT 1 FROM $TABLE_LIKES l WHERE l.trainer_id = t.id AND l.user_id = ?) as is_liked FROM $TABLE_TRAINERS t WHERE t.status = 'active' ORDER BY RANDOM() LIMIT 6", arrayOf(userId.toString()))
        if (c.moveToFirst()) { do { list.add(cursorToTrainer(c)) } while (c.moveToNext()) }
        c.close(); return list
    }

    fun getAllWorkouts(): List<Workout> {
        val list = mutableListOf<Workout>()
        val c = readableDatabase.rawQuery("SELECT * FROM $TABLE_WORKOUTS ORDER BY id DESC", null)
        if (c.moveToFirst()) { do { list.add(Workout(c.getInt(0), c.getInt(1), c.getString(2), c.getString(3), c.getString(4), c.getString(5), c.getString(6))) } while (c.moveToNext()) }
        c.close(); return list
    }

    fun getWorkoutCount(): Int {
        val c = readableDatabase.rawQuery("SELECT COUNT(*) FROM $TABLE_WORKOUTS", null)
        var count = 0; if (c.moveToFirst()) count = c.getInt(0); c.close(); return count
    }

    fun addWorkout(w: Workout): Boolean = writableDatabase.insert(TABLE_WORKOUTS, null, ContentValues().apply { put("trainer_id", w.trainer_id); put("workout_name", w.workout_name); put("sets_count", w.sets_count); put("reps_count", w.reps_count); put("muscle_group", w.muscle_group); put("video_url", w.video_url) }) != -1L
    fun updateWorkout(w: Workout): Boolean = writableDatabase.update(TABLE_WORKOUTS, ContentValues().apply { put("workout_name", w.workout_name); put("sets_count", w.sets_count); put("reps_count", w.reps_count); put("muscle_group", w.muscle_group); put("video_url", w.video_url) }, "id = ?", arrayOf(w.id.toString())) > 0
    fun getWorkoutById(id: Int): Workout? {
        val c = readableDatabase.rawQuery("SELECT * FROM $TABLE_WORKOUTS WHERE id = ?", arrayOf(id.toString()))
        var w: Workout? = null
        if (c.moveToFirst()) w = Workout(c.getInt(0), c.getInt(1), c.getString(2), c.getString(3), c.getString(4), c.getString(5), c.getString(6))
        c.close(); return w
    }

    fun searchTrainers(query: String, userId: Int): List<com.example.fitbody.model.Trainer> {
        val list = mutableListOf<com.example.fitbody.model.Trainer>()
        val sql = "SELECT DISTINCT t.*, (SELECT COUNT(*) FROM $TABLE_LIKES l WHERE l.trainer_id = t.id) as like_count, (SELECT 1 FROM $TABLE_LIKES l WHERE l.trainer_id = t.id AND l.user_id = ?) as is_liked FROM $TABLE_TRAINERS t LEFT JOIN $TABLE_WORKOUTS w ON t.id = w.trainer_id WHERE t.name LIKE ? OR t.specialty LIKE ? OR t.muscle LIKE ? OR w.workout_name LIKE ? OR w.muscle_group LIKE ?"
        val p = "%${query.trim()}%"; val cursor = readableDatabase.rawQuery(sql, arrayOf(userId.toString(), p, p, p, p, p))
        if (cursor.moveToFirst()) { do { list.add(cursorToTrainer(cursor)) } while (cursor.moveToNext()) }
        cursor.close(); return list
    }

    fun isUserEnrolled(userId: Int, trainerId: Int): Boolean {
        val cursor = readableDatabase.rawQuery("SELECT 1 FROM $TABLE_ENROLLMENTS WHERE user_id = ? AND trainer_id = ?", arrayOf(userId.toString(), trainerId.toString()))
        val exists = cursor.moveToFirst(); cursor.close(); return exists
    }
    fun enrollTrainer(userId: Int, trainerId: Int): Boolean {
        val date = java.text.SimpleDateFormat("dd/MM/yyyy").format(java.util.Date())
        return try { writableDatabase.insertWithOnConflict(TABLE_ENROLLMENTS, null, ContentValues().apply { put("user_id", userId); put("trainer_id", trainerId); put("enroll_date", date) }, SQLiteDatabase.CONFLICT_IGNORE) != -1L } catch (e: Exception) { false }
    }
    fun unenrollTrainer(userId: Int, trainerId: Int): Boolean = writableDatabase.delete(TABLE_ENROLLMENTS, "user_id = ? AND trainer_id = ?", arrayOf(userId.toString(), trainerId.toString())) > 0

    fun getSchedule(userId: Int): List<com.example.fitbody.model.Schedule> {
        val list = mutableListOf<com.example.fitbody.model.Schedule>()
        val cursor = readableDatabase.rawQuery("SELECT * FROM $TABLE_SCHEDULE WHERE user_id = ?", arrayOf(userId.toString()))
        if (cursor.moveToFirst()) { do { list.add(com.example.fitbody.model.Schedule(cursor.getInt(0), cursor.getInt(1), cursor.getString(2), cursor.getString(3), if (cursor.getInt(4) == 1) "completed" else "pending")) } while (cursor.moveToNext()) }
        cursor.close(); return list
    }
    fun addSchedule(userId: Int, day: String, plan: String): Long = writableDatabase.insert(TABLE_SCHEDULE, null, ContentValues().apply { put("user_id", userId); put("day_name", day); put("workout_plan", plan); put("is_completed", 0) })
    fun completeSchedule(id: Int): Boolean = writableDatabase.update(TABLE_SCHEDULE, ContentValues().apply { put("is_completed", 1) }, "id = ?", arrayOf(id.toString())) > 0
    fun deleteSchedule(id: Int): Boolean = writableDatabase.delete(TABLE_SCHEDULE, "id = ?", arrayOf(id.toString())) > 0

    fun getWorkoutStats(userId: Int): WorkoutStatsResponse {
        val cursor = readableDatabase.rawQuery("SELECT COUNT(*) FROM $TABLE_CHECKIN WHERE user_id = ?", arrayOf(userId.toString()))
        var count = 0; if (cursor.moveToFirst()) count = cursor.getInt(0); cursor.close()
        return WorkoutStatsResponse(true, count, count * 350, if (count > 0) count % 7 + 1 else 0, (count * 100 / 20).coerceAtMost(100))
    }

    fun addReview(uId: Int, tId: Int, r: Int, c: String): Boolean = writableDatabase.insert(TABLE_REVIEWS, null, ContentValues().apply { put("user_id", uId); put("trainer_id", tId); put("rating", r); put("comment", c); put("date", java.text.SimpleDateFormat("dd/MM/yyyy").format(java.util.Date())) }) != -1L
    fun getReviewsForTrainer(tId: Int): List<com.example.fitbody.model.Review> {
        val list = mutableListOf<com.example.fitbody.model.Review>()
        val c = readableDatabase.rawQuery("SELECT r.*, u.username FROM $TABLE_REVIEWS r JOIN $TABLE_USERS u ON r.user_id = u.id WHERE r.trainer_id = ? ORDER BY r.id DESC", arrayOf(tId.toString()))
        if (c.moveToFirst()) { do { list.add(com.example.fitbody.model.Review(c.getInt(0), c.getInt(1), c.getString(c.getColumnIndexOrThrow("username")), c.getInt(3), c.getInt(4), c.getString(5), c.getString(6))) } while (c.moveToNext()) }
        c.close(); return list
    }
}
