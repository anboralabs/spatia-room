package co.anbora.labs.spatiaroom.ui.main

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import co.anbora.labs.spatia.geometry.Point

import co.anbora.labs.spatiaroom.R
import co.anbora.labs.spatiaroom.data.AppDatabase
import co.anbora.labs.spatiaroom.data.model.Post
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainFragment : Fragment() {

    companion object {
        fun newInstance() = MainFragment()
    }

    private lateinit var appDatabase: AppDatabase

    val job = Job()
    val uiScope = CoroutineScope(Dispatchers.Main + job)

    private val viewModel: MainViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        appDatabase = AppDatabase.getInstance(requireContext())

        val spatia_version = view?.findViewById<TextView>(R.id.spatia_version)
        val proj4_version = view?.findViewById<TextView>(R.id.proj4_version)
        val geos_version = view?.findViewById<TextView>(R.id.geos_version)
        val polyline_txt = view?.findViewById<TextView>(R.id.polyline_txt)
        val distance_txt = view?.findViewById<TextView>(R.id.distance_txt)
        val azimuth_txt = view?.findViewById<TextView>(R.id.azimuth_txt)

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