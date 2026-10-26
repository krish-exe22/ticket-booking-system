from __future__ import annotations

import json
from datetime import datetime
from typing import Any

import requests
import streamlit as st


API_BASE = "http://localhost:8080/api"


def api_request(method: str, path: str, **kwargs: Any) -> Any:
    try:
        response = requests.request(method, f"{API_BASE}{path}", timeout=8, **kwargs)
        if response.ok:
            return response.json()
        try:
            detail = response.json().get("message", response.text)
        except ValueError:
            detail = response.text
        raise RuntimeError(f"Backend error ({response.status_code}): {detail}")
    except requests.RequestException as exc:
        raise RuntimeError(
            "The Spring Boot backend is unavailable. Start it on http://localhost:8080."
        ) from exc


def initialize_state() -> None:
    defaults = {
        "page": "Home",
        "movie": None,
        "shows": [],
        "show": None,
        "seats": [],
        "selected_seats": [],
        "user": None,
        "booking": None,
        "error": None,
    }
    for key, value in defaults.items():
        st.session_state.setdefault(key, value)


def reset_booking() -> None:
    for key in ("movie", "shows", "show", "seats", "selected_seats", "booking"):
        st.session_state[key] = None if key in {"movie", "show", "booking"} else []


def format_time(value: str) -> str:
    try:
        return datetime.fromisoformat(value).strftime("%a, %d %b · %I:%M %p")
    except ValueError:
        return value


def load_movies() -> list[dict[str, Any]]:
    return api_request("GET", "/movies")


def select_movie(movie: dict[str, Any]) -> None:
    st.session_state.movie = movie
    st.session_state.shows = api_request("GET", f"/movies/{movie['id']}/shows")
    st.session_state.page = "Movie"
    st.session_state.error = None


def select_show(show: dict[str, Any]) -> None:
    st.session_state.show = show
    st.session_state.seats = api_request("GET", f"/shows/{show['id']}/seats")
    st.session_state.selected_seats = []
    st.session_state.page = "Seats"


st.set_page_config(
    page_title="CINÉVAULT",
    page_icon="🎬",
    layout="wide",
    initial_sidebar_state="expanded",
)
initialize_state()

st.markdown(
    """
    <style>
    @import url('https://fonts.googleapis.com/css2?family=DM+Mono:wght@400;500&family=Space+Grotesk:wght@400;500;600;700&display=swap');
    :root { --red: #e50914; --ink: #080808; --panel: #151515; --muted: #989898; }
    .stApp { background: radial-gradient(circle at 88% 0%, #381015 0, #080808 32rem); color: #f7f7f7; }
    [data-testid="stHeader"] { background: rgba(8,8,8,.9); }
    [data-testid="stSidebar"] { background: #0d0d0d; border-right: 1px solid #282828; }
    .brand { font-family: 'Space Grotesk'; letter-spacing: -.07em; font-size: 2.2rem; font-weight: 700; }
    .brand span { color: var(--red); }
    .eyebrow { color: #d5ad5b; font-family: 'DM Mono'; font-size: .72rem; letter-spacing: .18em; text-transform: uppercase; }
    h1, h2, h3 { font-family: 'Space Grotesk'; letter-spacing: -.045em; }
    h1 { font-size: clamp(2.5rem, 5vw, 5.5rem); line-height: .98; }
    h2 { font-size: 2rem; }
    .hero { padding: 4rem 0 3rem; border-bottom: 1px solid #282828; margin-bottom: 2.5rem; }
    .hero-copy { max-width: 45rem; }
    .hero-copy p, .muted { color: var(--muted); font-size: 1.05rem; line-height: 1.7; }
    .panel { background: linear-gradient(145deg, #191919, #101010); border: 1px solid #2b2b2b; border-radius: 18px; padding: 1.35rem; }
    .movie-meta { color: var(--muted); font-size: .82rem; margin: .35rem 0 1rem; }
    .price { color: #e9c879; font-family: 'DM Mono'; font-size: .9rem; }
    .seat-screen { background: linear-gradient(90deg, transparent, var(--red), transparent); height: 3px; margin: 1rem auto 2rem; max-width: 34rem; box-shadow: 0 0 24px #e5091466; }
    .seat-screen-label { color: #777; font-family: 'DM Mono'; font-size: .7rem; letter-spacing: .25em; text-align: center; }
    .ticket { border: 1px solid #49332e; border-top: 4px solid var(--red); border-radius: 18px; background: linear-gradient(145deg, #211414, #101010); padding: 2rem; }
    .reference { color: #e9c879; font-family: 'DM Mono'; letter-spacing: .1em; }
    div.stButton > button { border-radius: 9px; border: 1px solid #3a3a3a; background: #1b1b1b; color: #fff; min-height: 2.7rem; }
    div.stButton > button:hover { border-color: var(--red); color: #fff; }
    div.stButton > button[kind="primary"] { background: var(--red); border-color: var(--red); }
    </style>
    """,
    unsafe_allow_html=True,
)

with st.sidebar:
    st.markdown('<div class="brand">CINÉ<span>VAULT</span></div>', unsafe_allow_html=True)
    st.caption("A premium cinema booking desk")
    st.divider()
    if st.button("⌂  Home", use_container_width=True):
        st.session_state.page = "Home"
    if st.button("◉  Browse movies", use_container_width=True):
        st.session_state.page = "Movies"
    if st.button("▣  My bookings", use_container_width=True):
        st.session_state.page = "Bookings"
    st.divider()
    st.caption("Backend-connected demo")
    st.caption("Only movies, shows, seats, users and bookings provided by the API are shown.")
    if st.session_state.user:
        st.success(f"Signed in as {st.session_state.user['name']}")

try:
    if st.session_state.page == "Home":
        st.markdown(
            '<div class="hero"><div class="eyebrow">The night starts here</div>'
            '<div class="hero-copy"><h1>Your seat to the extraordinary.</h1>'
            '<p>Discover what is playing, choose your showtime, and reserve your seats in a few cinematic steps.</p></div></div>',
            unsafe_allow_html=True,
        )
        movies = load_movies()
        if not movies:
            st.info("No movies are currently available from the backend.")
        else:
            featured = movies[0]
            col_a, col_b = st.columns([1.5, 1])
            with col_a:
                st.markdown('<div class="eyebrow">Now showing</div>', unsafe_allow_html=True)
                st.header(featured["title"])
                st.markdown(
                    f'<p class="muted">{featured["genre"]} · {featured["language"]} · '
                    f'{featured["durationMinutes"]} min · ★ {featured["rating"]:.1f}</p>',
                    unsafe_allow_html=True,
                )
                if st.button("Book this movie", type="primary"):
                    select_movie(featured)
                    st.rerun()
            with col_b:
                st.markdown(
                    f'<div class="panel"><div class="eyebrow">Featured release</div>'
                    f'<h2>{featured["title"]}</h2><p class="muted">Live showtimes and seat availability from your cinema backend.</p>'
                    f'<div class="price">RATING  {featured["rating"]:.1f} / 10</div></div>',
                    unsafe_allow_html=True,
                )
        st.subheader("Explore the current lineup")
        cols = st.columns(4)
        for index, movie in enumerate(movies[:8]):
            with cols[index % 4]:
                st.markdown(
                    f'<div class="panel"><div class="eyebrow">{movie["language"]}</div>'
                    f'<h3>{movie["title"]}</h3><div class="movie-meta">{movie["genre"]} · {movie["durationMinutes"]} min</div>'
                    f'<div class="price">★ {movie["rating"]:.1f}</div></div>',
                    unsafe_allow_html=True,
                )
                if st.button("View showtimes", key=f"home-{movie['id']}", use_container_width=True):
                    select_movie(movie)
                    st.rerun()

    elif st.session_state.page == "Movies":
        st.markdown('<div class="eyebrow">The vault</div>', unsafe_allow_html=True)
        st.title("Choose your next story")
        movies = load_movies()
        query = st.text_input("Search movies", placeholder="Search by title, genre or language")
        filtered = [
            movie for movie in movies
            if not query or query.lower() in json.dumps(movie).lower()
        ]
        for movie in filtered:
            left, right = st.columns([4, 1])
            with left:
                st.markdown(
                    f'<div class="panel"><h3>{movie["title"]}</h3><div class="movie-meta">'
                    f'{movie["genre"]} · {movie["language"]} · {movie["durationMinutes"]} min · ★ {movie["rating"]:.1f}'
                    f'</div></div>',
                    unsafe_allow_html=True,
                )
            with right:
                if st.button("Showtimes", key=f"movie-{movie['id']}", type="primary", use_container_width=True):
                    select_movie(movie)
                    st.rerun()
        if not filtered:
            st.info("No matching movies found.")

    elif st.session_state.page == "Movie":
        movie = st.session_state.movie
        st.button("← Back to movies", on_click=lambda: st.session_state.update(page="Movies"))
        st.markdown('<div class="eyebrow">Select a showtime</div>', unsafe_allow_html=True)
        st.title(movie["title"])
        st.markdown(
            f'<p class="muted">{movie["genre"]} · {movie["language"]} · {movie["durationMinutes"]} min · ★ {movie["rating"]:.1f}</p>',
            unsafe_allow_html=True,
        )
        if not st.session_state.shows:
            st.info("There are no shows for this movie.")
        for show in st.session_state.shows:
            left, middle, right = st.columns([3, 2, 1])
            with left:
                st.markdown(f"**{show['screen']}**  \n{format_time(show['startTime'])}")
            with middle:
                st.caption(f"{show['availableSeats']} of {show['totalSeats']} seats available")
            with right:
                st.markdown(f'<div class="price">₹{show["price"]}</div>', unsafe_allow_html=True)
                if st.button("Choose seats", key=f"show-{show['id']}", use_container_width=True):
                    select_show(show)
                    st.rerun()
            st.divider()

    elif st.session_state.page == "Seats":
        show = st.session_state.show
        st.markdown('<div class="eyebrow">Your cinema plan</div>', unsafe_allow_html=True)
        st.title("Pick your seats")
        st.caption(f"{show['movieTitle']} · {show['screen']} · {format_time(show['startTime'])}")
        st.markdown('<div class="seat-screen"></div><div class="seat-screen-label">SCREEN</div>', unsafe_allow_html=True)
        available = {seat["seatNumber"] for seat in st.session_state.seats if seat["status"] == "AVAILABLE"}
        booked = {seat["seatNumber"] for seat in st.session_state.seats if seat["status"] == "BOOKED"}
        rows: dict[str, list[str]] = {}
        for seat in available | booked:
            rows.setdefault(seat[0], []).append(seat)
        for row, row_seats in sorted(rows.items()):
            st.markdown(f"**{row}**")
            cols = st.columns(len(sorted(row_seats)))
            for col, seat_number in zip(cols, sorted(row_seats)):
                with col:
                    is_booked = seat_number in booked
                    label = f"✕ {seat_number}" if is_booked else ("✓ " if seat_number in st.session_state.selected_seats else "") + seat_number
                    if st.button(label, key=f"seat-{seat_number}", disabled=is_booked, use_container_width=True):
                        if seat_number in st.session_state.selected_seats:
                            st.session_state.selected_seats.remove(seat_number)
                        else:
                            st.session_state.selected_seats.append(seat_number)
                        st.rerun()
        st.divider()
        total = len(st.session_state.selected_seats) * float(show["price"])
        st.write(f"Selected: {', '.join(st.session_state.selected_seats) or 'None'}")
        st.metric("Ticket subtotal", f"₹{total:.2f}")
        if st.button("Continue to checkout", type="primary", disabled=not st.session_state.selected_seats):
            st.session_state.page = "Checkout"
            st.rerun()

    elif st.session_state.page == "Checkout":
        show = st.session_state.show
        st.markdown('<div class="eyebrow">Final step</div>', unsafe_allow_html=True)
        st.title("Complete your booking")
        left, right = st.columns([1.4, 1])
        with left:
            with st.form("checkout"):
                name = st.text_input("Full name")
                email = st.text_input("Email address")
                method = st.radio("Demo payment method", ["CARD", "UPI"], horizontal=True)
                reference = st.text_input("Demo payment reference", placeholder="card-demo-123 or name@upi")
                st.caption("This is a simulated payment flow. Do not enter real card or banking details.")
                submitted = st.form_submit_button("Confirm booking", type="primary", use_container_width=True)
            if submitted:
                if not name.strip() or not email.strip() or not reference.strip():
                    st.error("Name, email and demo payment reference are required.")
                else:
                    user = api_request("POST", "/users", json={"name": name.strip(), "email": email.strip()})
                    booking = api_request(
                        "POST",
                        "/bookings",
                        json={
                            "userId": user["id"],
                            "showId": show["id"],
                            "seatNumbers": st.session_state.selected_seats,
                            "paymentMethod": method,
                            "paymentReference": reference.strip(),
                        },
                    )
                    st.session_state.user = user
                    st.session_state.booking = booking
                    st.session_state.page = "Confirmation"
                    st.rerun()
        with right:
            st.markdown(
                f'<div class="panel"><div class="eyebrow">Booking summary</div><h3>{show["movieTitle"]}</h3>'
                f'<p class="muted">{show["screen"]}<br>{format_time(show["startTime"])}</p>'
                f'<p>Seats: {", ".join(st.session_state.selected_seats)}</p>'
                f'<div class="price">TOTAL  ₹{len(st.session_state.selected_seats) * float(show["price"]):.2f}</div></div>',
                unsafe_allow_html=True,
            )

    elif st.session_state.page == "Confirmation":
        booking = st.session_state.booking
        st.markdown('<div class="eyebrow">Reservation complete</div>', unsafe_allow_html=True)
        st.title("Your cinema pass is ready.")
        if booking:
            st.markdown(
                f'<div class="ticket"><div class="eyebrow">CINÉVAULT / CONFIRMED</div>'
                f'<h2>{booking["movieTitle"]}</h2><p class="muted">{format_time(booking["showTime"])} · {booking["screen"]}</p>'
                f'<p>Seats: <strong>{", ".join(booking["seats"])}</strong></p>'
                f'<p class="reference">REFERENCE  CV-{booking["id"]:06d}</p>'
                f'<h3>₹{booking["totalAmount"]}</h3></div>',
                unsafe_allow_html=True,
            )
        if st.button("Browse more movies", type="primary"):
            reset_booking()
            st.session_state.page = "Movies"
            st.rerun()

    elif st.session_state.page == "Bookings":
        st.markdown('<div class="eyebrow">Your history</div>', unsafe_allow_html=True)
        st.title("My bookings")
        user_id = st.number_input("User ID", min_value=1, step=1, value=st.session_state.user["id"] if st.session_state.user else 1)
        bookings = api_request("GET", f"/users/{int(user_id)}/bookings")
        if not bookings:
            st.info("No bookings found for this user.")
        for booking in bookings:
            with st.container(border=True):
                col_a, col_b = st.columns([3, 1])
                with col_a:
                    st.subheader(booking["movieTitle"])
                    st.caption(f"{format_time(booking['showTime'])} · {booking['screen']} · Seats {', '.join(booking['seats'])}")
                with col_b:
                    st.metric("Total", f"₹{booking['totalAmount']}")
                    st.caption(booking["status"])
                    if booking["status"] == "CONFIRMED" and st.button("Cancel", key=f"cancel-{booking['id']}"):
                        api_request("POST", f"/bookings/{booking['id']}/cancel")
                        st.rerun()

except RuntimeError as exc:
    st.error(str(exc))
