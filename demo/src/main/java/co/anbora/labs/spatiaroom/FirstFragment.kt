package co.anbora.labs.spatiaroom

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import co.anbora.labs.spatia.geometry.Point
import co.anbora.labs.spatiaroom.data.AppDatabase
import co.anbora.labs.spatiaroom.data.model.Post
import kotlinx.coroutines.*

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private lateinit var appDatabase: AppDatabase

    val job = Job()
    val uiScope = CoroutineScope(Dispatchers.Main + job)

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_first, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        appDatabase = AppDatabase.getInstance(requireContext())

        val spatia_version = getView()?.findViewById<TextView>(R.id.spatia_version)
        val proj4_version = getView()?.findViewById<TextView>(R.id.proj4_version)
        val geos_version = getView()?.findViewById<TextView>(R.id.geos_version)
        val polyline_txt = getView()?.findViewById<TextView>(R.id.polyline_txt)
        val distance_txt = getView()?.findViewById<TextView>(R.id.distance_txt)
        val azimuth_txt = getView()?.findViewById<TextView>(R.id.azimuth_txt)

        uiScope.launch(Dispatchers.IO) {
            val post1 = Post(1, "prueba", "darwin", "spatia", "test.img", Point(0.0, 0.0))
            val post2 = Post(2, "prueba2", null, "spatia2", "test2.img", Point(-122.084801, 37.422131))

            val listPost = listOf(post1, post2)

            appDatabase.getPostsDao().insertPosts(listPost)

            val spatiaVersion = appDatabase.getPostsDao().getSpatiaVersion()
            val proj4Version = appDatabase.getPostsDao().getProj4Version()
            val geosVersion = appDatabase.getPostsDao().getGeosVersion()
            val makePoliline = appDatabase.getPostsDao().getMakePolyline()
            val distance = appDatabase.getPostsDao().getDistance()
            val post = appDatabase.getPostsDao().getAllPostsList()
            val azimuth = appDatabase.getPostsDao().testAzimuth()

            withContext(Dispatchers.Main) {
                spatia_version?.text = spatiaVersion
                proj4_version?.text = proj4Version
                geos_version?.text = geosVersion
                polyline_txt?.text = makePoliline
                distance_txt?.text = distance.toString()
                azimuth_txt?.text = azimuth.toString()
            }
        }
    }

    override fun onDestroy() {
        job.cancel()
        super.onDestroy()
    }
}
