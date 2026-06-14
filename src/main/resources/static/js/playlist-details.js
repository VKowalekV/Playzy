const modal = document.getElementById('spotifySearchModal');
const searchInput = document.getElementById('spotifySearchInput');
const resultsContainer = document.getElementById('spotify-results');
const loadingIndicator = document.getElementById('spotify-loading');
const playlistId = document.getElementById('playlist-page-container').dataset.playlistId;

let debounceTimer;
let currentSearchId = 0;

function getCsrfToken() {
    return {
        token: document.querySelector('meta[name="_csrf"]')?.content,
        header: document.querySelector('meta[name="_csrf_header"]')?.content
    };
}

async function fetchSpotifyTracks(query) {
    const response = await fetch(`/api/spotify/search?q=${encodeURIComponent(query)}`);
    if (!response.ok) throw new Error('Niepoprawna odpowiedź z serwera');
    return response.json();
}

async function saveTrackToPlaylist(track) {
    const csrf = getCsrfToken();
    if (!csrf.token || !csrf.header) {
        throw new Error("Błąd CSRF. Odśwież stronę.");
    }
    
    const response = await fetch(`/api/playlists/${playlistId}/tracks`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            [csrf.header]: csrf.token
        },
        body: JSON.stringify(track)
    });
    
    if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.error || "Wystąpił błąd podczas dodawania utworu.");
    }
    
    return response.json();
}

function openSpotifyModal() {
    modal.classList.add('show');
    searchInput.focus();
}

function closeSpotifyModal() {
    modal.classList.remove('show');
    resultsContainer.innerHTML = '<div class="spotify-empty-state">Zacznij pisać, aby przeszukać bazę Spotify...</div>';
}

searchInput.addEventListener('input', (e) => {
    clearTimeout(debounceTimer);
    const query = e.target.value.trim();
    
    if (query.length < 2) {
        resultsContainer.innerHTML = '<div class="spotify-empty-state">Zacznij pisać, aby przeszukać bazę Spotify...</div>';
        loadingIndicator.style.display = 'none';
        return;
    }

    loadingIndicator.style.display = 'block';
    resultsContainer.innerHTML = '';

    debounceTimer = setTimeout(() => {
        currentSearchId++;
        handleSearch(query, currentSearchId);
    }, 500);
});

async function handleSearch(query, searchId) {
    try {
        const tracks = await fetchSpotifyTracks(query);
        if (searchId !== currentSearchId) return;
        
        loadingIndicator.style.display = 'none';
        renderSearchResults(tracks);
    } catch (error) {
        console.error('Błąd podczas wyszukiwania w Spotify:', error);
        if (searchId === currentSearchId) {
            loadingIndicator.style.display = 'none';
            resultsContainer.innerHTML = '<div class="spotify-empty-state spotify-error-state">Wystąpił błąd podczas wyszukiwania.</div>';
        }
    }
}

function getExistingTrackIds() {
    const iframes = document.querySelectorAll('.playlist-track-content iframe');
    const ids = new Set();
    iframes.forEach(iframe => {
        const match = iframe.src.match(/track\/([a-zA-Z0-9]+)/);
        if (match && match[1]) ids.add(match[1]);
    });
    return ids;
}

function renderSearchResults(tracks) {
    resultsContainer.innerHTML = '';
    if (tracks.length === 0) {
        resultsContainer.innerHTML = '<div class="spotify-empty-state">Nic nie znaleziono</div>';
        return;
    }

    const existingIds = getExistingTrackIds();
    tracks.forEach(track => {
        const trackEl = document.createElement('div');
        trackEl.className = 'spotify-track-item';
        
        const isAdded = existingIds.has(track.id);
        const btnHtml = isAdded 
            ? `<button class="nav-btn spotify-track-add-btn added" disabled>Dodano</button>`
            : `<button class="nav-btn primary spotify-track-add-btn" onclick='handleAddTrack(this, ${JSON.stringify(track).replace(/'/g, "&#39;")})'>Dodaj</button>`;
        
        trackEl.innerHTML = `
            <div class="playlist-track-embed-container">
                <iframe src="https://open.spotify.com/embed/track/${track.id}?utm_source=generator&theme=0" class="playlist-track-iframe" frameBorder="0" allowfullscreen="" allow="autoplay; clipboard-write; encrypted-media; fullscreen; picture-in-picture" loading="lazy"></iframe>
            </div>
            ${btnHtml}
        `;
        resultsContainer.appendChild(trackEl);
    });
}

function appendTrackToPlaylistDOM(savedTrack) {
    const container = document.querySelector('.tracks-container');
    const emptyState = container.querySelector('.empty-state');
    if (emptyState) emptyState.remove();

    const item = document.createElement('div');
    item.className = 'playlist-track-wrapper';
    item.id = `track-row-${savedTrack.id}`;
    
    const index = container.querySelectorAll('.playlist-track-wrapper').length + 1;
    const canEdit = document.querySelector('.playlist-details-add-btn') !== null;
    const csrfToken = getCsrfToken().token;
    
    item.innerHTML = `
        <div class="playlist-track-content">
            <div class="playlist-track-index">${index}</div>
            <div class="playlist-track-embed-container">
                <iframe src="https://open.spotify.com/embed/track/${savedTrack.spotifyId}?utm_source=generator&theme=0" class="playlist-track-iframe" frameBorder="0" allowfullscreen="" allow="autoplay; clipboard-write; encrypted-media; fullscreen; picture-in-picture" loading="lazy"></iframe>
            </div>
            ${canEdit ? `<div>
                <form action="/playlists/${playlistId}/tracks/${savedTrack.id}/delete" method="post" class="playlist-track-delete-form" onsubmit="return confirm('Czy na pewno chcesz usunąć ten utwór z playlisty?');">
                    <input type="hidden" name="_csrf" value="${csrfToken}"/>
                    <button type="submit" class="btn-delete">Usuń</button>
                </form>
            </div>` : ''}
        </div>
    `;
    container.appendChild(item);
    return index;
}

function updatePlaylistStatsDOM(newTrackCount, durationMinutesToAdd) {
    const totalTracksEl = document.getElementById('total-tracks');
    if (totalTracksEl) {
        totalTracksEl.innerText = `${newTrackCount} utworów`;
    }

    const totalDurationEl = document.getElementById('total-duration');
    if (totalDurationEl) {
        let currentTotalMinutes = parseFloat(totalDurationEl.getAttribute('data-raw-duration') || 0);
        currentTotalMinutes += durationMinutesToAdd;
        totalDurationEl.setAttribute('data-raw-duration', currentTotalMinutes);
        
        const totalSeconds = Math.round(currentTotalMinutes * 60);
        const hours = Math.floor(totalSeconds / 3600);
        const minutes = Math.floor((totalSeconds % 3600) / 60);
        const seconds = totalSeconds % 60;
        
        if (hours > 0) {
            totalDurationEl.innerText = `${hours} godz. ${minutes} min`;
        } else {
            totalDurationEl.innerText = `${minutes} min ${seconds} s`;
        }
    }
}

function setButtonAddingState(btn) {
    btn.innerText = '...';
    btn.disabled = true;
}

function setButtonAddedState(btn) {
    btn.innerText = "Dodano";
    btn.classList.remove('primary');
    btn.classList.add('added');
}

function setButtonErrorState(btn, originalText) {
    btn.innerText = "Błąd";
    setTimeout(() => {
        btn.innerText = originalText;
        btn.disabled = false;
    }, 2000);
}

async function handleAddTrack(btn, track) {
    const originalText = btn.innerText;
    setButtonAddingState(btn);

    try {
        const savedTrack = await saveTrackToPlaylist(track);
        
        const newTrackCount = appendTrackToPlaylistDOM(savedTrack);
        updatePlaylistStatsDOM(newTrackCount, savedTrack.durationMinutes);
        
        setButtonAddedState(btn);
    } catch (error) {
        console.error('Błąd podczas dodawania utworu:', error);
        alert(error.message || "Wystąpił błąd podczas komunikacji z serwerem.");
        setButtonErrorState(btn, originalText);
    }
}
