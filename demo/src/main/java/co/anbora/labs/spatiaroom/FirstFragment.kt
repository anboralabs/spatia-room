package co.anbora.labs.spatiaroom

import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.fragment.findNavController
import co.anbora.labs.spatiaroom.data.AppDatabase
import co.anbora.labs.spatiaroom.data.model.Post
import kotlinx.android.synthetic.main.fragment_first.*

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private lateinit var appDatabase: AppDatabase

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

        AsyncTask.execute(Runnable {

            val post1 = Post(1, "prueba", "darwin", "spatia", "test.img")
            val post2 = Post(2, "prueba2", null, "spatia2", "test2.img")

            val listPost = listOf(post1, post2)

            appDatabase.getPostsDao().insertPosts(listPost)

            val list = appDatabase.getPostsDao().getPolygon()

            val spatiaVersion = appDatabase.getPostsDao().getSpatiaVersion()
            val proj4Version = appDatabase.getPostsDao().getProj4Version()
            val geosVersion = appDatabase.getPostsDao().getGeosVersion()
            val makePoliline = appDatabase.getPostsDao().getMakePolyline()
            val distance = appDatabase.getPostsDao().getDistance()
            val post = appDatabase.getPostsDao().getAllPostsList()

            spatia_version.text = spatiaVersion
            proj4_version.text = proj4Version
            geos_version.text = geosVersion
            polyline_txt.text = makePoliline
            distance_txt.text = distance.toString()

        })
    }
}
