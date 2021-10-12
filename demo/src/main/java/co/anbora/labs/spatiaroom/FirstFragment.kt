package co.anbora.labs.spatiaroom

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import co.anbora.labs.spatiaroom.data.AppDatabase
import co.anbora.labs.spatiaroom.data.dao.ContractDao
import co.anbora.labs.spatiaroom.data.dao.PostsDao
import co.anbora.labs.spatiaroom.data.model.Contract
import co.anbora.labs.spatiaroom.data.model.Post
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_first.*
import kotlinx.coroutines.*
import javax.inject.Inject

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
@AndroidEntryPoint
class FirstFragment : Fragment() {

    @Inject
    lateinit var job: Job

    @Inject
    lateinit var uiScope: CoroutineScope

    @Inject
    lateinit var postDao: PostsDao

    @Inject
    lateinit var contractDao: ContractDao

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_first, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        uiScope.launch(Dispatchers.IO) {
            val post1 = Post(1, "prueba", "darwin", "spatia", "test.img")
            val post2 = Post(2, "prueba2", null, "spatia2", "test2.img")

            val contract = Contract(name = "Contract", id = 1, machine_id = 2)
            val contract2 = Contract(name = "Contract 2", id = 2, machine_id = 3)

            val listPost = listOf(post1, post2)

            postDao.insertPosts(listPost)
            contractDao.insert(contract)
            contractDao.insert(contract2)

            val contracts = contractDao.allContracts().blockingGet()

            val spatiaVersion = postDao.getSpatiaVersion()
            val proj4Version = postDao.getProj4Version()
            val geosVersion = postDao.getGeosVersion()
            val makePoliline = postDao.getMakePolyline()
            val distance = postDao.getDistance()
            val post = postDao.getAllPostsList()

            withContext(Dispatchers.Main) {
                spatia_version.text = spatiaVersion
                proj4_version.text = proj4Version
                geos_version.text = geosVersion
                polyline_txt.text = makePoliline
                distance_txt.text = distance.toString()
            }
        }
    }

    override fun onDestroy() {
        job.cancel()
        super.onDestroy()
    }
}
