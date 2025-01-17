/*
 * Copyright 2020 Miklos Vajna. All rights reserved.
 * Use of this source code is governed by a BSD-style license that can be
 * found in the LICENSE file.
 */

package hu.vmiklos.plees_tracker

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.ContentResolver
import android.content.Context
import android.text.format.DateFormat
import android.widget.RatingBar
import android.widget.TextView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.util.Calendar
import java.util.Date
import kotlinx.coroutines.launch

/**
 * This is the view model of SleepActivity, providing coroutine scopes.
 */
class SleepViewModel : ViewModel() {

    fun showSleep(activity: SleepActivity, sid: Int) {
        val viewModel = this
        viewModelScope.launch {
            val sleep = DataModel.getSleepById(sid)

            val start = activity.findViewById<TextView>(R.id.sleep_start)
            start.text = DataModel.formatTimestamp(Date(sleep.start))
            val stop = activity.findViewById<TextView>(R.id.sleep_stop)
            stop.text = DataModel.formatTimestamp(Date(sleep.stop))
            val rating = activity.findViewById<RatingBar>(R.id.sleep_item_rating)
            rating.rating = sleep.rating.toFloat()
            rating.onRatingBarChangeListener = SleepRateCallback(activity, viewModel, sleep)
        }
    }

    fun editSleep(
        activity: SleepActivity,
        sid: Int,
        isStart: Boolean,
        context: Context,
        cr: ContentResolver
    ) {
        viewModelScope.launch {
            val sleep = DataModel.getSleepById(sid)

            val dateTime = Calendar.getInstance()
            dateTime.time =
                if (isStart) {
                    Date(sleep.start)
                } else {
                    Date(sleep.stop)
                }
            DatePickerDialog(
                activity,
                { _/*view*/, year, monthOfYear, dayOfMonth ->
                    dateTime.set(year, monthOfYear, dayOfMonth)
                    TimePickerDialog(
                        activity,
                        { _/*view*/, hourOfDay, minute ->
                            dateTime[Calendar.HOUR_OF_DAY] = hourOfDay
                            dateTime[Calendar.MINUTE] = minute
                            if (isStart) {
                                sleep.start = dateTime.time.time
                            } else {
                                sleep.stop = dateTime.time.time
                            }
                            updateSleep(activity, sleep, context, cr)
                        },
                        dateTime[Calendar.HOUR_OF_DAY], dateTime[Calendar.MINUTE],
                        /*is24HourView=*/DateFormat.is24HourFormat(activity)
                    ).show()
                },
                dateTime[Calendar.YEAR], dateTime[Calendar.MONTH], dateTime[Calendar.DATE]
            ).show()
        }
    }

    private fun updateSleep(
        activity: SleepActivity,
        sleep: Sleep,
        context: Context,
        cr: ContentResolver
    ) {
        viewModelScope.launch {
            DataModel.updateSleep(sleep)
            DataModel.backupSleeps(context, cr)
            showSleep(activity, sleep.sid)
        }
    }

    fun updateSleep(activity: SleepActivity, sleep: Sleep) {
        viewModelScope.launch {
            DataModel.updateSleep(sleep)
            showSleep(activity, sleep.sid)
        }
    }
}

/* vim:set shiftwidth=4 softtabstop=4 expandtab: */
