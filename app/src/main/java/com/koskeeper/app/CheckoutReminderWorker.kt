package com.koskeeper.app

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class CheckoutReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val db = AppDatabase.getDatabase(applicationContext)
        val bookingDao = db.bookingDao()

        val today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val now = LocalTime.now()

        val activeBookings = bookingDao.getActiveLengkapSync()

        for (booking in activeBookings) {
            try {
                val partsIn = booking.jamCheckin.split(":")
                val checkinTime = LocalTime.of(partsIn[0].toInt(), partsIn[1].toInt())
                val checkinLimit = checkinTime.minusMinutes(30)

                if (booking.tanggalCheckin == today &&
                    now.isBefore(checkinTime) &&
                    now.isAfter(checkinLimit)
                ) {
                    NotificationHelper.sendCheckinReminder(
                        context = applicationContext,
                        id = booking.id.toInt() + 100000,
                        namaKamar = "${booking.nomorKamar} (${booking.tipeKamar})",
                        namaTamu = booking.namaLengkap,
                        jamCheckin = booking.jamCheckin
                    )
                }
            } catch (_: Exception) {}

            try {
                val partsOut = booking.jamCheckout.split(":")
                val checkoutTime = LocalTime.of(partsOut[0].toInt(), partsOut[1].toInt())
                val checkoutLimit = checkoutTime.minusMinutes(10)

                if (booking.tanggalCheckout == today &&
                    now.isBefore(checkoutTime) &&
                    now.isAfter(checkoutLimit)
                ) {
                    NotificationHelper.sendCheckoutReminder(
                        context = applicationContext,
                        id = booking.id.toInt(),
                        namaKamar = "${booking.nomorKamar} (${booking.tipeKamar})",
                        namaTamu = booking.namaLengkap,
                        jamCheckout = booking.jamCheckout
                    )
                }
            } catch (_: Exception) {}
        }

        return Result.success()
    }
}
