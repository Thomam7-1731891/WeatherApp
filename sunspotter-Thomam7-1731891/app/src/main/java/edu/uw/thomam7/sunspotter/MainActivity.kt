package edu.uw.thomam7.sunspotter

import android.util.Log
import org.json.JSONException
import android.content.Context
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View.inflate
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import com.android.volley.*
import com.android.volley.toolbox.*
import kotlinx.android.synthetic.main.activity_main.*
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.NetworkImageView
import kotlinx.android.synthetic.main.listviewlayout.*
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import androidx.core.app.ComponentActivity
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val enterCity = search
        val findButton = search_button
        findButton.setOnClickListener {
            getWeatherInformation(enterCity)
        }
    }

    fun getWeatherInformation(city: EditText) {
        val call_2 = "http://api.openweathermap.org/data/2.5/forecast?q=Seattle&units=imperial&format=json&APPID=6ce003b030bff85e9d8da1f691163f0d"
        val call = "http://api.openweathermap.org/data/2.5/forecast?q=${city.text}&units=imperial&format=json&APPID=6ce003b030bff85e9d8da1f691163f0d"
        val apiKey = getString(R.string.OPEN_WEATHER_MAP_API_KEY)
        val requester = Requester.weatherRequestQueue(applicationContext)
        var request = JsonObjectRequest(
            Request.Method.GET, call, null,
            Response.Listener { response ->
                val getJSON = response.getJSONArray("list")
                var parse = handleData(getJSON)
            },
            Response.ErrorListener { error ->
                announcement.text = "Invalid Search"
                subAnnounce.text = "Please try a valid City!"
                list_view.visibility = View.INVISIBLE
                weather.setImageResource(R.drawable.ic_highlight_off_black_24dp)
                Log.e("Sorry", error.toString())
            })
        requester.add(request)
    }

    fun handleData(data: JSONArray) {
        weather.setImageResource(R.drawable.ic_check_circle_black_24dp)
        val initialWeather = data.getJSONObject(0).getJSONArray("weather").getJSONObject(0).getString("main")
        val initialTime = data.getJSONObject(0).getString("dt_txt")
        announcement.text = "There will be $initialWeather"
        subAnnounce.text = "At $initialTime"
        list_view.visibility = View.VISIBLE
        try {
            println(data).toString()
            val listOfForecasts = mutableListOf<ForecastData>()
            for (i in 1 until data.length()) //iterate through array object
            {
                val dataArrayObject = data.getJSONObject(i) //get ith object from array
                val getWeather = dataArrayObject.getJSONArray("weather").getJSONObject(0).getString("main")
                val getTemp = dataArrayObject.getJSONObject("main").getString("temp")
                val getTime = dataArrayObject.getString("dt_txt")
                var getIcon = "icon" + dataArrayObject.getJSONArray("weather").getJSONObject(0).getString("icon")
                var setIcon: Int = resources.getIdentifier(getIcon, "drawable", this.packageName)
                var convertToDrawableIcon = ContextCompat.getDrawable(applicationContext, setIcon) as Drawable
                val specificForecast = ForecastData(getWeather, getTime, getTemp, convertToDrawableIcon)
                listOfForecasts.add(specificForecast)
            }
            val adapter = ForecastAdapter(applicationContext, listOfForecasts)
            val listView: ListView = list_view
            listView.setAdapter(adapter)
            adapter.addAll(listOfForecasts)
        } catch (e: JSONException) {
            Log.e("Sorry", "Error parsing json") //Android log the error
        }
    }

    class Requester() {
        companion object {
            val request: RequestQueue? = null
            fun weatherRequestQueue(context: Context): RequestQueue {
                return Volley.newRequestQueue(context)
            }
        }
    }

    class ForecastAdapter(context: Context, dataList: List<ForecastData>) : ArrayAdapter<ForecastData>(context, R.layout.listviewlayout, dataList) {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var view = convertView
            val forecast = getItem(position)
            val holder: ViewHolder
            if (view == null) {
                view = LayoutInflater.from(context).inflate(R.layout.listviewlayout, parent, false)
                holder = ViewHolder()
                holder.iconView = view.findViewById(R.id.weather_icon)
                holder.weatherView = view.findViewById(R.id.weather_type) as TextView
                holder.timeView = view.findViewById(R.id.time)
                holder.tempView = view.findViewById(R.id.temp)
                view.tag = holder
            } else {
                holder = view.tag as ViewHolder
            }
            holder.iconView?.setImageDrawable(forecast?.icon)
            holder.weatherView?.text = forecast?.weather
            holder.timeView?.text = forecast?.time
            holder.tempView?.text = forecast?.temp
            return view as View
        }
    }

    data class ForecastData(var weather: String, var time: String, var temp: String, var icon: Drawable)

    private class ViewHolder() {
        var iconView: ImageView? = null
        var weatherView: TextView? = null
        var timeView: TextView? = null
        var tempView: TextView? = null
    }

}
