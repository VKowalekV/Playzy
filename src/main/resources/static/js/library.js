const createPlaylistModal = document.getElementById('createPlaylistModal');
const playlistNameInput = document.getElementById('name');

function switchTab(event, tabId) {
    document.querySelectorAll('.tab-content').forEach(tab => {
        tab.classList.remove('active');
    });

    document.querySelectorAll('.tab-btn').forEach(btn => {
        btn.classList.remove('active');
    });
    
    document.getElementById(tabId).classList.add('active');
    event.currentTarget.classList.add('active');
}

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
