function getCsrfToken() {
    return {
        token: document.querySelector('meta[name="_csrf"]')?.content,
        header: document.querySelector('meta[name="_csrf_header"]')?.content
    };
}

function handleUnauthorized() {
    window.location.href = '/login';
}

async function apiRatePlaylist(playlistId, isLike) {
    const csrf = getCsrfToken();
    if (!csrf.token || !csrf.header) {
        handleUnauthorized();
        return null;
    }

    const response = await fetch(`/api/playlists/${playlistId}/rate`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
            [csrf.header]: csrf.token
        },
        body: new URLSearchParams({ like: isLike })
    });

    if (response.status === 401) {
        handleUnauthorized();
        return null;
    }

    if (!response.ok) {
        throw new Error('Błąd podczas oceniania playlisty');
    }

    return response.json();
}

async function apiToggleFollow(playlistId) {
    const csrf = getCsrfToken();
    if (!csrf.token || !csrf.header) {
        handleUnauthorized();
        return null;
    }

    const response = await fetch(`/api/playlists/${playlistId}/toggle-follow`, {
        method: 'POST',
        headers: {
            [csrf.header]: csrf.token
        }
    });

    if (response.status === 401) {
        handleUnauthorized();
        return null;
    }

    if (!response.ok) {
        throw new Error('Błąd podczas zmiany statusu obserwowania');
    }

    return response.json();
}

function updateRatingDOM(playlistId, data) {
    const likesSpan = document.getElementById(`likes-count-${playlistId}`);
    const dislikesSpan = document.getElementById(`dislikes-count-${playlistId}`);
    
    if (likesSpan) likesSpan.innerText = data.likesCount;
    if (dislikesSpan) dislikesSpan.innerText = data.dislikesCount;

    const likeBtn = document.getElementById(`like-btn-${playlistId}`);
    const dislikeBtn = document.getElementById(`dislike-btn-${playlistId}`);
    
    if (likeBtn) {
        if (data.userRating === true) likeBtn.classList.add('active');
        else likeBtn.classList.remove('active');
    }
    if (dislikeBtn) {
        if (data.userRating === false) dislikeBtn.classList.add('active');
        else dislikeBtn.classList.remove('active');
    }
}

function updateFollowButtonDOM(playlistId, isFollowed) {
    const btn = document.getElementById(`follow-btn-${playlistId}`);
    if (!btn) return null;

    btn.innerText = isFollowed ? 'Odobserwuj' : 'Obserwuj';
    
    if (!isFollowed) {
        btn.classList.add('active');
    } else {
        btn.classList.remove('active');
    }
    
    return btn;
}

function removePlaylistCardIfUnfollowedInLibrary(btn, isFollowed) {
    if (window.location.pathname !== '/library/followed' || isFollowed) {
        return;
    }

    const card = btn.closest('.playlist-card');
    if (!card) return;
    
    const container = card.parentElement;
    card.remove();
    
    if (container && container.children.length === 0) {
        const tabContent = container.closest('.tab-content');
        if (tabContent) {
            let emptyState = tabContent.querySelector('.empty-state');
            if (!emptyState) {
                emptyState = document.createElement('p');
                emptyState.className = 'empty-state';
                emptyState.innerText = 'Nie obserwujesz żadnych playlist.';
                tabContent.insertBefore(emptyState, container);
            }
            emptyState.style.display = 'block';
        }
    }
}

async function ratePlaylist(event, playlistId, isLike) {
    event.preventDefault();
    try {
        const data = await apiRatePlaylist(playlistId, isLike);
        if (data) {
            updateRatingDOM(playlistId, data);
        }
    } catch (error) {
        console.error('Błąd podczas oceniania playlisty:', error);
    }
}

async function toggleFollow(event, playlistId) {
    event.preventDefault();
    try {
        const data = await apiToggleFollow(playlistId);
        if (data) {
            const btn = updateFollowButtonDOM(playlistId, data.followed);
            if (btn) {
                removePlaylistCardIfUnfollowedInLibrary(btn, data.followed);
            }
        }
    } catch (error) {
        console.error('Błąd podczas zmiany statusu obserwowania:', error);
    }
}

function goToPlaylist(event, playlistId) {
    if (event.target.closest('button') || event.target.closest('form') || event.target.closest('a')) {
        return;
    }
    window.location.href = `/playlists/${playlistId}`;
}
