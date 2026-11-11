package fr.maxime.launcher

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {

    private data class AppItem(val label: String, val icon: Drawable, val intent: Intent)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = ScrollView(this).apply { setBackgroundColor(Color.parseColor("#0b0d12")) }
        val column = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(56), dp(48), dp(56), dp(48))
        }
        root.addView(column)

        column.addView(TextView(this).apply {
            text = "Accueil"
            textSize = 26f
            setTextColor(Color.WHITE)
        })

        val grid = GridLayout(this).apply { columnCount = 6 }
        loadApps().forEach { grid.addView(makeTile(it)) }
        column.addView(
            grid,
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                .apply { topMargin = dp(32) }
        )

        setContentView(root)
        grid.post { grid.getChildAt(0)?.requestFocus() }
    }

    /** Liste toutes les applis TV installées, sauf celle-ci. */
    private fun loadApps(): List<AppItem> {
        val query = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER)
        return packageManager.queryIntentActivities(query, 0)
            .filter { it.activityInfo.packageName != packageName }
            .map { info ->
                AppItem(
                    label = info.loadLabel(packageManager).toString(),
                    icon = info.loadIcon(packageManager),
                    intent = Intent(Intent.ACTION_MAIN)
                        .addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER)
                        .setClassName(info.activityInfo.packageName, info.activityInfo.name)
                )
            }
            .sortedBy { it.label.lowercase() }
    }

    private fun makeTile(item: AppItem): View {
        val normalBg = GradientDrawable().apply {
            cornerRadius = dp(16).toFloat()
            setColor(Color.parseColor("#1c1f27"))
        }
        val focusBg = GradientDrawable().apply {
            cornerRadius = dp(16).toFloat()
            setColor(Color.parseColor("#2b3140"))
            setStroke(dp(3), Color.WHITE)
        }

        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            isFocusable = true
            isClickable = true
            background = normalBg
            setPadding(dp(16), dp(20), dp(16), dp(16))
            layoutParams = GridLayout.LayoutParams().apply {
                width = dp(190)
                height = dp(170)
                setMargins(dp(10), dp(10), dp(10), dp(10))
            }

            addView(ImageView(this@MainActivity).apply {
                setImageDrawable(item.icon)
                layoutParams = LinearLayout.LayoutParams(dp(88), dp(88))
            })
            addView(TextView(this@MainActivity).apply {
                text = item.label
                textSize = 16f
                setTextColor(Color.WHITE)
                gravity = Gravity.CENTER
                maxLines = 1
                setPadding(0, dp(12), 0, 0)
            })

            setOnFocusChangeListener { v, hasFocus ->
                background = if (hasFocus) focusBg else normalBg
                val scale = if (hasFocus) 1.08f else 1f
                v.animate().scaleX(scale).scaleY(scale).setDuration(150).start()
            }
            setOnClickListener {
                try {
                    startActivity(item.intent)
                } catch (e: Exception) {
                    Toast.makeText(this@MainActivity, "Impossible d'ouvrir ${item.label}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun dp(value: Int): Int =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value.toFloat(), resources.displayMetrics).toInt()
}
