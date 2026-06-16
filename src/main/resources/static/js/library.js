const createPlaylistModal = document.getElementById('createPlaylistModal');
const playlistNameInput = document.getElementById('name');



function openCreatePlaylistModal() {
    if (createPlaylistModal) {
        createPlaylistModal.classList.add('show');
        if (playlistNameInput) {
            playlistNameInput.focus();
        }
    }
}

function closeCreatePlaylistModal() {
    if (createPlaylistModal) {
        createPlaylistModal.classList.remove('show');
    }
}
