function getCsrfToken() {
    const token = document.querySelector('meta[name="_csrf"]')?.content;
    const header = document.querySelector('meta[name="_csrf_header"]')?.content;
    return { token, header };
}

async function ratePlaylist(event, playlistId, isLike) {
    event.preventDefault();
    const csrf = getCsrfToken();
    if (!csrf.token || !csrf.header) {
        window.location.href = '/login';
        return;
    }

    try {
        const response = await fetch(`/api/playlists/${playlistId}/rate`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
                [csrf.header]: csrf.token
            },
            body: new URLSearchParams({ like: isLike })
        });

        if (response.status === 401) {
            window.location.href = '/login';
            return;
        }

        if (response.ok) {
            const data = await response.json();
            
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
    } catch (error) {
        console.error('Error rating playlist:', error);
    }
}

async function toggleFollow(event, playlistId) {
    event.preventDefault();
    const csrf = getCsrfToken();
    if (!csrf.token || !csrf.header) {
        window.location.href = '/login';
        return;
    }

    try {
        const response = await fetch(`/api/playlists/${playlistId}/toggle-follow`, {
            method: 'POST',
            headers: {
                [csrf.header]: csrf.token
            }
        });

        if (response.status === 401) {
            window.location.href = '/login';
            return;
        }

        if (response.ok) {
            const data = await response.json();
            const btn = document.getElementById(`follow-btn-${playlistId}`);
            if (btn) {
                btn.innerText = data.followed ? 'Odobserwuj' : 'Obserwuj';

                if (window.location.pathname === '/library' && !data.followed) {
                    const card = btn.closest('.playlist-card');
                    if (card) {
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
                }
            }
        }
    } catch (error) {
        console.error('Error toggling follow:', error);
    }
}
