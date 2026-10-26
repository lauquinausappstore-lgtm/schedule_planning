package com.dayline.planner

import android.app.*
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.*
import android.widget.*
import org.json.JSONArray
import org.json.JSONObject
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.UUID

data class PlanEvent(val id: String, val title: String, val date: LocalDate, val start: LocalTime,
    val end: LocalTime, val category: String, val notes: String)

class MainActivity : Activity() {
    private val ink = Color.rgb(30, 49, 42)
    private val green = Color.rgb(51, 107, 88)
    private val muted = Color.rgb(111, 124, 114)
    private val paper = Color.rgb(247, 248, 244)
    private var selected = LocalDate.now()
    private var month = YearMonth.now()
    private val events = mutableListOf<PlanEvent>()
    private var storageReadable = true
    private lateinit var content: LinearLayout
    private val categories = arrayOf("Personal", "Work", "Health", "Study")
    private val clockFormat = DateTimeFormatter.ofPattern("HH:mm")
    private fun dp(n: Int) = (n * resources.displayMetrics.density).toInt()
    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        state?.getString("selected")?.let { selected = LocalDate.parse(it) }
        month = state?.getString("month")?.let { YearMonth.parse(it) } ?: YearMonth.from(selected)
        loadEvents()
        render()
    }
    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("selected", selected.toString()); outState.putString("month", month.toString())
        super.onSaveInstanceState(outState)
    }
    private fun rounded(color: Int, radius: Int = 20) = GradientDrawable().apply {
        setColor(color); cornerRadius = dp(radius).toFloat()
    }
    private fun label(value: String, size: Float = 16f, color: Int = ink, bold: Boolean = false) = TextView(this).apply {
        text = value; textSize = size; setTextColor(color)
        if (bold) setTypeface(null, Typeface.BOLD)
    }
    private fun column() = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
    private fun row() = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
    private fun space(parent: LinearLayout, height: Int) { parent.addView(View(this), LinearLayout.LayoutParams(1, dp(height))) }
    private fun button(value: String, action: () -> Unit) = Button(this).apply {
        text = value; isAllCaps = false; textSize = 14f; setTextColor(green)
        background = rounded(Color.rgb(232, 239, 229), 14)
        setOnClickListener { action() }
    }
    private fun render() {
        val root = column().apply { setBackgroundColor(paper) }
        root.setOnApplyWindowInsetsListener { view, insets ->
            view.setPadding(insets.systemWindowInsetLeft, insets.systemWindowInsetTop,
                insets.systemWindowInsetRight, insets.systemWindowInsetBottom)
            insets
        }
        val scroll = ScrollView(this).apply { isFillViewport = true }
        content = column().apply { setPadding(dp(22), dp(22), dp(22), dp(28)) }
        scroll.addView(content); root.addView(scroll); setContentView(root)
        val header = row()
        val brand = column()
        brand.addView(label("DAYLINE", 13f, green, true)); space(brand, 6)
        brand.addView(label("Make room for life.", 27f, ink, true))
        header.addView(brand, LinearLayout.LayoutParams(0, -2, 1f))
        content.addView(header); space(content, 8)
        content.addView(label("A little structure. A clearer day.", 14f, muted)); space(content, 24)
        val nav = row()
        nav.addView(button("‹") { month = month.minusMonths(1); render() }, LinearLayout.LayoutParams(dp(44), dp(44)))
        nav.addView(label(month.format(DateTimeFormatter.ofPattern("MMMM yyyy")), 19f, ink, true).apply { gravity = Gravity.CENTER }, LinearLayout.LayoutParams(0, -2, 1f))
        nav.addView(button("›") { month = month.plusMonths(1); render() }, LinearLayout.LayoutParams(dp(44), dp(44)))
        content.addView(nav); space(content, 16)
        val days = row()
        listOf("M", "T", "W", "T", "F", "S", "S").forEach {
            days.addView(label(it, 12f, muted, true).apply { gravity = Gravity.CENTER }, LinearLayout.LayoutParams(0, dp(28), 1f))
        }
        content.addView(days)
        val offset = month.atDay(1).dayOfWeek.value - 1
        val cells = ((offset + month.lengthOfMonth() + 6) / 7) * 7
        for (week in 0 until cells / 7) {
            val line = row()
            for (weekday in 0..6) {
                val number = week * 7 + weekday - offset + 1
                val cell = label("", 16f).apply { gravity = Gravity.CENTER }
                if (number in 1..month.lengthOfMonth()) {
                    val date = month.atDay(number)
                    cell.text = "$number" + if (events.any { it.date == date }) "\n•" else ""
                    if (date == selected) { cell.background = rounded(green, 15); cell.setTextColor(Color.WHITE) }
                    else if (date == LocalDate.now()) { cell.background = rounded(Color.rgb(228, 237, 223), 15) }
                    cell.contentDescription = date.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy"))
                    cell.setOnClickListener { selected = date; render() }
                }
                line.addView(cell, LinearLayout.LayoutParams(0, dp(51), 1f).apply { setMargins(dp(2), dp(2), dp(2), dp(2)) })
            }
            content.addView(line)
        }
        space(content, 24)
        val agenda = row()
        val caption = column()
        caption.addView(label(if (selected == LocalDate.now()) "Today’s agenda" else "Your agenda", 23f, ink, true))
        caption.addView(label(selected.format(DateTimeFormatter.ofPattern("EEEE, MMMM d")), 14f, muted))
        agenda.addView(caption, LinearLayout.LayoutParams(0, -2, 1f))
        agenda.addView(button("Today") { selected = LocalDate.now(); month = YearMonth.from(selected); render() }, LinearLayout.LayoutParams(dp(70), dp(44)))
        content.addView(agenda); space(content, 16)
        val todayEvents = events.filter { it.date == selected }.sortedBy { it.start }
        if (todayEvents.isEmpty()) {
            val empty = column().apply { background = rounded(Color.WHITE); setPadding(dp(22), dp(28), dp(22), dp(28)) }
            empty.addView(label("A little breathing room", 20f, ink, true)); space(empty, 8)
            empty.addView(label("No plans for this day yet. Add something to look forward to.", 15f, muted))
            content.addView(empty)
        } else todayEvents.forEach { event ->
            val card = column().apply { background = rounded(Color.WHITE); setPadding(dp(18), dp(16), dp(18), dp(16)) }
            card.addView(label("${event.start.format(clockFormat)} — ${event.end.format(clockFormat)}  ·  ${event.category}", 13f, green, true))
            space(card, 7); card.addView(label(event.title, 19f, ink, true))
            if (event.notes.isNotBlank()) { space(card, 6); card.addView(label(event.notes, 14f, muted)) }
            space(card, 8); card.addView(label("Tap to edit", 12f, muted))
            card.setOnClickListener { editEvent(event) }
            content.addView(card, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(10) })
        }
        space(content, 18)
        content.addView(button("+  Add a plan") { editEvent(null) }.apply { setTextColor(Color.WHITE); background = rounded(green, 17) }, LinearLayout.LayoutParams(-1, dp(54)))
        space(content, 16); content.addView(label("Your plans stay on this device.", 12f, muted).apply { gravity = Gravity.CENTER })
    }
    private fun editEvent(existing: PlanEvent?) {
        var date = existing?.date ?: selected
        var start = existing?.start ?: LocalTime.of(9, 0)
        var end = existing?.end ?: LocalTime.of(10, 0)
        val form = column().apply { setPadding(dp(24), dp(8), dp(24), dp(12)) }
        fun input(hintValue: String, value: String, multiline: Boolean = false) = EditText(this).apply {
            hint = hintValue; setText(value); textSize = 16f
            inputType = android.text.InputType.TYPE_CLASS_TEXT or if (multiline) android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE else android.text.InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
            if (!multiline) setSingleLine(true)
        }
        val title = input("What’s the plan?", existing?.title ?: "")
        val notes = input("Notes (optional)", existing?.notes ?: "", true)
        form.addView(label("TITLE", 12f, muted, true)); form.addView(title)
        val dateButton = button(date.toString()) {}
        dateButton.setOnClickListener { DatePickerDialog(this, { _, y, m, d ->
            date = LocalDate.of(y, m + 1, d); dateButton.text = date.toString()
        }, date.year, date.monthValue - 1, date.dayOfMonth).show() }
        space(form, 12); form.addView(label("DATE", 12f, muted, true)); form.addView(dateButton, LinearLayout.LayoutParams(-1, dp(48)))
        val times = row()
        val from = button(start.format(clockFormat)) {}; val to = button(end.format(clockFormat)) {}
        from.setOnClickListener { TimePickerDialog(this, { _, h, m -> start = LocalTime.of(h, m); from.text = start.format(clockFormat) }, start.hour, start.minute, true).show() }
        to.setOnClickListener { TimePickerDialog(this, { _, h, m -> end = LocalTime.of(h, m); to.text = end.format(clockFormat) }, end.hour, end.minute, true).show() }
        times.addView(from, LinearLayout.LayoutParams(0, dp(48), 1f)); times.addView(label("  to  ", 14f, muted)); times.addView(to, LinearLayout.LayoutParams(0, dp(48), 1f))
        space(form, 12); form.addView(label("TIME", 12f, muted, true)); form.addView(times)
        val category = Spinner(this).apply { adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_dropdown_item, categories); setSelection(categories.indexOf(existing?.category).coerceAtLeast(0)) }
        space(form, 12); form.addView(label("CATEGORY", 12f, muted, true)); form.addView(category)
        space(form, 12); form.addView(notes)
        val dialog = AlertDialog.Builder(this).setTitle(if (existing == null) "New plan" else "Edit plan")
            .setView(ScrollView(this).apply { addView(form) }).setPositiveButton("Save", null).setNegativeButton("Cancel", null)
            .apply { if (existing != null) setNeutralButton("Delete", null) }.create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                if (title.text.toString().isBlank()) { title.error = "Please enter a title"; return@setOnClickListener }
                if (!end.isAfter(start)) { Toast.makeText(this, "End time must be after start time on the same day", Toast.LENGTH_LONG).show(); return@setOnClickListener }
                val event = PlanEvent(existing?.id ?: UUID.randomUUID().toString(), title.text.toString().trim(), date, start, end, category.selectedItem.toString(), notes.text.toString().trim())
                val next = events.filter { it.id != event.id } + event
                if (saveEvents(next)) { events.clear(); events.addAll(next); selected = date; month = YearMonth.from(date); dialog.dismiss(); render() }
            }
            if (existing != null) dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener {
                AlertDialog.Builder(this).setTitle("Delete this plan?").setMessage(existing.title).setNegativeButton("Keep", null)
                    .setPositiveButton("Delete") { _, _ ->
                        val next = events.filter { it.id != existing.id }
                        if (saveEvents(next)) { events.clear(); events.addAll(next); dialog.dismiss(); render() }
                    }.show()
            }
        }
        dialog.show()
    }
    private fun loadEvents() {
        val raw = getSharedPreferences("dayline", MODE_PRIVATE).getString("events", "[]") ?: "[]"
        try {
            val array = JSONArray(raw)
            for (i in 0 until array.length()) {
                val o = array.getJSONObject(i)
                events.add(PlanEvent(o.getString("id"), o.getString("title"), LocalDate.parse(o.getString("date")), LocalTime.parse(o.getString("start")), LocalTime.parse(o.getString("end")), o.getString("category"), o.optString("notes")))
            }
        } catch (_: Exception) {
            storageReadable = false
            events.clear()
            AlertDialog.Builder(this).setTitle("Couldn’t read saved plans").setMessage("Your stored data has been preserved. Changes are disabled to protect it. Restore the data or clear app storage in Android Settings to start fresh.").setPositiveButton("OK", null).show()
        }
    }
    private fun saveEvents(values: List<PlanEvent>): Boolean {
        if (!storageReadable) {
            Toast.makeText(this, "Saved data could not be read. Changes are disabled.", Toast.LENGTH_LONG).show()
            return false
        }
        val array = JSONArray()
        values.forEach { e -> array.put(JSONObject().put("id", e.id).put("title", e.title).put("date", e.date.toString()).put("start", e.start.toString()).put("end", e.end.toString()).put("category", e.category).put("notes", e.notes)) }
        val saved = getSharedPreferences("dayline", MODE_PRIVATE).edit().putString("events", array.toString()).commit()
        if (!saved) Toast.makeText(this, "Couldn’t save. Please try again.", Toast.LENGTH_LONG).show()
        return saved
    }
}
