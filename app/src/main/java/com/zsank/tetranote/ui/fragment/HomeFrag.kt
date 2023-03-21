package com.zsank.tetranote.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.zsank.tetranote.FolderViewModel
import com.zsank.tetranote.NoteViewModel
import com.zsank.tetranote.R
import com.zsank.tetranote.data.Folder
import com.zsank.tetranote.data.Note
import com.zsank.tetranote.databinding.FragmentHomeBinding
import com.zsank.tetranote.ui.folder.FolderAdapter
import dagger.hilt.android.AndroidEntryPoint

private const val TAG = "test"

@AndroidEntryPoint
class HomeFrag : Fragment() {

	//TODO: Add current Folder list, use just string like mxPlayer

	private val noteViewModel: NoteViewModel by viewModels()
	private val folderViewModel: FolderViewModel by viewModels()
	private lateinit var note: Note
	private lateinit var binding: FragmentHomeBinding
	private val navigationArgs: HomeFragArgs by navArgs()
	private var receivedParentFolderId: Int = 0
	private var folderList = listOf<Folder>()
	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		// Inflate the layout for this fragment
		binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)
		setHasOptionsMenu(true)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		receivedParentFolderId = navigationArgs.parentFolderId

		val lamda: (Note) -> Unit = { passedNote ->
			val action = HomeFragDirections.actionHomeFragToEditNoteFrag(passedNote.id!!)
			findNavController().navigate(action)
		}
		val noteAdapter = NoteAdapter(lamda)
		binding.rcyViewHome.adapter = noteAdapter
		binding.rcyViewHome.isNestedScrollingEnabled = false

//		noteViewModel.allNotes.observe(this.viewLifecycleOwner) { noteList ->
//			noteAdapter.submitList(
//				noteList
//			)
//		}
		noteViewModel.allNoteInFolder(receivedParentFolderId).observe(this.viewLifecycleOwner) {
			noteAdapter.submitList(it)
		}


		val reloadFragment: (parentId: Int) -> Unit = { parentId ->
			Log.d(TAG, "reloadFragment: parentId:$parentId ")
			val action = HomeFragDirections.actionHomeFragSelf(parentId)
			findNavController().navigate(action)
		}
		val folderAdapter = FolderAdapter(reloadFragment)
		binding.rcyViewFolder.isNestedScrollingEnabled = false
		binding.rcyViewFolder.adapter = folderAdapter
		folderViewModel.getFolders(receivedParentFolderId).observe(this.viewLifecycleOwner) {
			folderList = it
			folderAdapter.submitList(it)

		}
		//TODO: SetUp diffUtil for Folder, since for contentcheck it is set to false


		binding.AddNoteFab.setOnClickListener {
			addNote()
		}
		binding.AddFolderFab.setOnClickListener {
			val action = HomeFragDirections.actionHomeFragToNewFolderFrag(receivedParentFolderId)
			findNavController().navigate(action)

//			addFolder()
//			val newFragment = NewFolderFrag()
//			val supportFragmentManager = parentFragmentManager
//			newFragment.show(supportFragmentManager, "game")
//			Log.d(TAG, "${supportFragmentManager.fragments}")

		}

	}

	private fun addFolder() {
		folderViewModel.insertFolder(Folder(null, "Folder$receivedParentFolderId", receivedParentFolderId))
	}
	//TODO: Folder Creation can also take place inside HomeFrag, dialog will only send the folder name


	override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
		super.onCreateOptionsMenu(menu, inflater)
		inflater.inflate(R.menu.menu_note_home, menu)
	}

	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		return when (item.itemId) {
			R.id.AboutMenu -> {
				navigateToAbout()
				true

			}
			R.id.AddNoteMenu -> {
				addNote()
				true
			}
			else -> super.onOptionsItemSelected(item)
		}
	}

	private fun addNote() {
		val action = HomeFragDirections.actionHomeFragToCreateNoteFrag(receivedParentFolderId)
		findNavController().navigate(action)
	}

	private fun navigateToAbout() {
		val action = HomeFragDirections.actionHomeFragToAboutFrag()
		findNavController().navigate(action)
	}
}

