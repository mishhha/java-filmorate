package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;

import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.film.*;
import ru.yandex.practicum.filmorate.model.user.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.director.DirectorDbStorage;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.mappers.DirectorRowMapper;
import ru.yandex.practicum.filmorate.storage.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.storage.mappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.storage.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@JdbcTest
@Import({UserDbStorage.class, UserRowMapper.class, FilmDbStorage.class, FilmRowMapper.class, GenreRowMapper.class,
		UserService.class, DirectorDbStorage.class, DirectorRowMapper.class})
@AutoConfigureTestDatabase
class FilmorateApplicationTests {

	@Autowired
	@Qualifier("userDbStorage")
	private UserStorage userStorage;

	@Autowired
	@Qualifier("filmDbStorage")
	private FilmStorage filmStorage;

	@Autowired
	@Qualifier("directorDbStorage")
	private DirectorStorage directorStorage;

	@Autowired
	private UserService userService;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	// 									Тесты для userDbStorage

	// Удаление пользователя
	@Test
	public void testDeleteUserById() {
		Long userId = 1L;

		userStorage.deleteUserById(userId);

		assertThrows(
			NotFoundException.class,
			() -> userStorage.getUserById(userId),
			"Ожидается, что после удаления пользователя его получение по ID вызовет NotFoundException"
		);
	}

	// Получить пользователя по ID
    @Test
	public void testFindUserById() {
		User user = userStorage.getUserById(1L);

		assertThat(user)
			.isNotNull()
			.hasFieldOrPropertyWithValue("id", 1L);
	}

	// Получение всех пользователей.
	@Test
	void testGetUsers() {
		List<User> users = userStorage.getUsers();

		assertThat(users).isNotNull();
		assertThat(users).isNotEmpty();
		assertThat(users).hasSize(3);
		assertThat(users.get(0).getEmail()).isEqualTo("test@mail.ru");
	}

	// Создание пользователя и проверка наличия его в БД.
	@Test
	void testAddUser() {
		User newUser = new User();
		newUser.setEmail("newuser@mail.ru");
		newUser.setLogin("newuser_login");
		newUser.setName("New User");
		newUser.setBirthday(LocalDate.of(1995, 5, 5));

		User created = userStorage.addUser(newUser);

		assertThat(created).isNotNull();
		assertThat(created.getId()).isNotNull();
		assertThat(created.getEmail()).isEqualTo("newuser@mail.ru");
		assertThat(created.getLogin()).isEqualTo("newuser_login");
		assertThat(created.getName()).isEqualTo("New User");
		assertThat(created.getBirthday()).isEqualTo(LocalDate.of(1995, 5, 5));

		User fromDb = userStorage.getUserById(created.getId());
		assertThat(fromDb).isNotNull();
		assertThat(fromDb.getEmail()).isEqualTo("newuser@mail.ru");
	}

	// Обновление данных пользователя и проверка этих обновлений в БД.
	@Test
	void testUpdateUser() {
		User userToUpdate = new User();
		userToUpdate.setId(1L);
		userToUpdate.setEmail("updated@mail.ru");
		userToUpdate.setLogin("updated_login");
		userToUpdate.setName("Updated Name");
		userToUpdate.setBirthday(LocalDate.of(1995, 5, 5));

		User updated = userStorage.updateUser(userToUpdate);

		assertThat(updated).isNotNull();
		assertThat(updated.getId()).isEqualTo(1L);
		assertThat(updated.getName()).isEqualTo("Updated Name");
		assertThat(updated.getEmail()).isEqualTo("updated@mail.ru");
		assertThat(updated.getLogin()).isEqualTo("updated_login");
		assertThat(updated.getBirthday()).isEqualTo(LocalDate.of(1995, 5, 5));

		User fromDb = userStorage.getUserById(1L);
		assertThat(fromDb).isNotNull();
		assertThat(fromDb.getName()).isEqualTo("Updated Name");
		assertThat(fromDb.getEmail()).isEqualTo("updated@mail.ru");
	}

	// Удаление дружбы
	@Test
	void testDeleteFriend() {
		// 1. Создаём дружбу
		userStorage.addFriend(1L, 2L);

		// 2. Проверяем, что друг появился
		List<User> friendsBefore = userStorage.getFriends(1L);
		assertThat(friendsBefore).isNotEmpty().hasSize(1);
		assertThat(friendsBefore.get(0).getId()).isEqualTo(2L);

		// 3. Удаляем друга (метод ничего не возвращает — и это ок!)
		userStorage.deleteFriend(1L, 2L);  // ← void

		// 4. ✅ Проверяем результат по состоянию: друга больше нет
		List<User> friendsAfter = userStorage.getFriends(1L);
		assertThat(friendsAfter).isEmpty();
	}

	// Добавление друга
	@Test
	void testAddFriend() {

		User friend = userStorage.addFriend(1L, 2L);

		assertThat(friend).isNotNull();
		assertThat(friend.getId()).isEqualTo(2L);

		List<User> friends = userStorage.getFriends(1L);
		assertThat(friends).isNotEmpty().hasSize(1);
		assertThat(friends.get(0).getId()).isEqualTo(2L);
	}

	// Проверка добавления дружбы и соответствия друзей.
	@Test
	void testGetFriends() {
		userStorage.addFriend(1L, 2L);
		userStorage.addFriend(1L, 3L);

		List<User> friends = userStorage.getFriends(1L);

		assertThat(friends).isNotNull();
		assertThat(friends).isNotEmpty();
		assertThat(friends).hasSize(2);

		List<Long> friendIds = friends.stream()
			.map(User::getId)
			.toList();
		assertThat(friendIds).containsExactlyInAnyOrder(2L, 3L);
	}

	// Получение общих друзей должно вернуть список общих друзей
	@Test
	void testGetCommonFriends() {

		userStorage.addFriend(1L, 3L);
		userStorage.addFriend(2L, 3L);

		List<User> commonFriends = userStorage.getCommonFriends(1L, 2L);

		assertThat(commonFriends).isNotNull();
		assertThat(commonFriends).isNotEmpty();
		assertThat(commonFriends).hasSize(1);
		assertThat(commonFriends.get(0).getId()).isEqualTo(3L);
	}


	// Поиск по неправильному ID
	@Test
	public void testFindUserById_notFound() {
		assertThatThrownBy(() -> userStorage.getUserById(999L))
			.isInstanceOf(NotFoundException.class)
			.hasMessageContaining("не найден");
	}

	//										Тесты для filmDbStorage

	// Удаление фильма по ID
	@Test
	public void testDeleteFilmById() {
		Long filmId = 1L;

		filmStorage.deleteFilmById(filmId);

		assertThrows(
			NotFoundException.class,
			() -> filmStorage.getFilmById(filmId),
			"Ожидается, что после удаления фильма его получение по ID вызовет NotFoundException"
		);
	}

	// Получение списка фильмов.
	@Test
	void testGetFilms() {
		// Act
		List<Film> films = filmStorage.getFilms();

		// Assert
		assertThat(films).isNotNull();
		assertThat(films).isNotEmpty();
		assertThat(films).hasSize(4);
		assertThat(films.get(0).getName()).isEqualTo("Test Film");
	}

	// Создание фильма и проверка наличия его в БД
	@Test
	void testAddFilm() {

		Film newFilm = new Film();
		newFilm.setName("New Film");
		newFilm.setDescription("New Description");
		newFilm.setReleaseDate(LocalDate.of(2024, 1, 1));
		newFilm.setDuration(120);

		RatingMpa ratingG = new RatingMpa();
		ratingG.setId(1L);

		newFilm.setRating(ratingG);

		Genre comedy = new Genre();
		comedy.setId(1L);

		Genre drama = new Genre();
		drama.setId(2L);

		newFilm.setGenres(List.of(comedy, drama));

		// установка режиссёров
		Director director1 = new Director();
		director1.setId(1L);

		Director director2 = new Director();
		director2.setId(2L);

		newFilm.setDirectors(List.of(director1, director2));

		Film created = filmStorage.addFilm(newFilm);

		assertThat(created).isNotNull();
		assertThat(created.getId()).isNotNull();
		assertThat(created.getName()).isEqualTo("New Film");
		assertThat(created.getDescription()).isEqualTo("New Description");
		assertThat(created.getDuration()).isEqualTo(120);
		assertThat(created.getReleaseDate()).isEqualTo(LocalDate.of(2024, 1, 1));

		Film fromDb = filmStorage.getFilmById(created.getId());
		assertThat(fromDb).isNotNull();
		assertThat(fromDb.getName()).isEqualTo("New Film");
	}

	// Получение фильма по ID должно вернуть фильм.
	@Test
	void testGetFilmById() {
		Film film = filmStorage.getFilmById(1L);

		assertThat(film).isNotNull();
		assertThat(film.getId()).isEqualTo(1L);
		assertThat(film.getName()).isEqualTo("Test Film");
		assertThat(film.getDescription()).isEqualTo("Test Description");
		assertThat(film.getDuration()).isEqualTo(120);
		assertThat(film.getReleaseDate()).isEqualTo(LocalDate.of(2020, 1, 1));
		assertThat(film.getGenres()).isNotEmpty();
		assertThat(film.getGenres()).extracting(Genre::getId).contains(1L);
		assertThat(film.getGenres()).extracting(Genre::getName).contains("Комедия");
		assertThat(film.getDirectors()).isNotEmpty();
		assertThat(film.getDirectors()).extracting(Director::getId).contains(1L);
		assertThat(film.getDirectors()).extracting(Director::getName).contains("Стивен Спилберг");
	}

	// Обновление данных фильма и проверка обновления в БД
	@Test
	void testUpdateFilm() {
		Genre drama = new Genre();
		drama.setId(2L);

		Genre action = new Genre();
		action.setId(6L);

		RatingMpa ratingPg13 = new RatingMpa();
		ratingPg13.setId(3L);

		Director director1 = new Director();
		director1.setId(1L);

		Director director2 = new Director();
		director2.setId(3L);

		Film filmToUpdate = new Film();
		filmToUpdate.setId(1L);
		filmToUpdate.setName("Updated Film");
		filmToUpdate.setDescription("Updated Description");
		filmToUpdate.setReleaseDate(LocalDate.of(2024, 6, 15));
		filmToUpdate.setDuration(150);
		filmToUpdate.setGenres(List.of(drama, action));
		filmToUpdate.setRating(ratingPg13);
		filmToUpdate.setDirectors(List.of(director1, director2));

		Film updated = filmStorage.updateFilm(filmToUpdate);

		assertThat(updated).isNotNull();
		assertThat(updated.getId()).isEqualTo(1L);
		assertThat(updated.getName()).isEqualTo("Updated Film");

		assertThat(updated.getGenres()).extracting(Genre::getName)
			.containsExactlyInAnyOrder("Драма", "Боевик");

		assertThat(updated.getRating()).isNotNull();
		assertThat(updated.getRating().getId()).isEqualTo(3L);
		assertThat(updated.getRating().getName()).isEqualTo("PG-13");
		assertThat(updated.getDirectors()).extracting(Director::getName)
				.containsExactlyInAnyOrder("Стивен Спилберг", "Квентин Тарантино");

		Film fromDb = filmStorage.getFilmById(1L);
		assertThat(fromDb.getGenres()).extracting(Genre::getName)
			.containsExactlyInAnyOrder("Драма", "Боевик");
		assertThat(fromDb.getRating().getId()).isEqualTo(3L);
		assertThat(fromDb.getDirectors()).extracting(Director::getName)
				.containsExactlyInAnyOrder("Стивен Спилберг", "Квентин Тарантино");
	}

	// Добавление лайка должно увеличить счётчик лайков
	@Test
	void testAddLike() {
		filmStorage.addLike(1L, 1L);

		Film film = filmStorage.getFilmById(1L);
		assertThat(film).isNotNull();
		assertThat(film.getLikes()).isEqualTo(2);

		Integer likesInDb = jdbcTemplate.queryForObject(
			"SELECT COUNT(*) FROM likes WHERE film_id = ? AND user_id = ?",
			Integer.class,
			1L, 1L
		);
		assertThat(likesInDb).isEqualTo(1);
	}

	// Удаление лайка должно уменьшить счётчик лайков
	@Test
	void testRemoveLike() {
		filmStorage.addLike(1L, 1L);

		// Проверяем, что лайк добавился
		Film filmBefore = filmStorage.getFilmById(1L);
		assertThat(filmBefore.getLikes()).isEqualTo(2);

		filmStorage.removeLike(1L, 1L);

		Film filmAfter = filmStorage.getFilmById(1L);
		assertThat(filmAfter).isNotNull();
		assertThat(filmAfter.getLikes()).isEqualTo(1);

		Integer likesInDb = jdbcTemplate.queryForObject(
			"SELECT COUNT(*) FROM likes WHERE film_id = ? AND user_id = ?",
			Integer.class,
			1L, 1L
		);
		assertThat(likesInDb).isEqualTo(0);
	}

	// Поиск несуществующего фильма должен выбросить NotFoundException
	@Test
	void testGetFilmById_notFound() {
		assertThatThrownBy(() -> filmStorage.getFilmById(999L))
			.isInstanceOf(NotFoundException.class)
			.hasMessageContaining("не найден");
	}

	// Получение общих фильмов
	@Test
	void testGetCommonFilms() {
		List<Film> commonFilms = filmStorage.getCommonFilms(1L, 2L);

		assertThat(commonFilms).isNotNull();
		assertThat(commonFilms).isNotEmpty();
		assertThat(commonFilms).hasSize(2);
		assertThat(commonFilms.getFirst().getId()).isEqualTo(2L);
	}

	// Получение рекомендаций по несуществующему пользователю
	@Test
	void testGetRecommendations_notFound() {
		assertThatThrownBy(() -> userService.getRecommendations(999L))
				.isInstanceOf(NotFoundException.class)
				.hasMessageContaining("не найден");
	}

	// Получение рекомендаций для пользователя
	@Test
	void testGetRecommendations() {
		List<Film> films = userService.getRecommendations(3L);
		assertTrue(films.size() == 2);
	}

	// Получение списка фильмов определённого режиссёра, отсортированные по лайкам
	@Test
	void testGetDirectorFilms_sortedByLikes() {
		List<Film> films = filmStorage.getDirectorFilms(1L, DirectorFilmsSort.LIKES);

		assertThat(films).isNotNull();
		assertThat(films).isNotEmpty();
		assertThat(films).hasSize(3);
		assertThat(films.get(0).getName()).isEqualTo("Чебурашка");
	}

	// Получение списка фильмов определённого режиссёра, отсортированные по годам
	@Test
	void testGetDirectorFilms_sortedByYear() {
		List<Film> films = filmStorage.getDirectorFilms(2L, DirectorFilmsSort.YEAR);

		assertThat(films).isNotNull();
		assertThat(films).isNotEmpty();
		assertThat(films).hasSize(3);
		assertThat(films.get(0).getName()).isEqualTo("Бриллиантовая рука");
	}

	// 									Тесты для directorDbStorage

	// получение списка всех режиссёров
	@Test
	void testGetDirectors() {
		List<Director> directors = directorStorage.getDirectors();

		assertThat(directors).isNotNull();
		assertThat(directors).isNotEmpty();
		assertThat(directors).hasSize(3);
		assertThat(directors.get(0).getName()).isEqualTo("Стивен Спилберг");
	}

	// Получение режиссёра по ID
	@Test
	void testGetDirectorById() {
		Director director = directorStorage.getDirectorById(2L);

		assertThat(director).isNotNull();
		assertThat(director.getId()).isEqualTo(2L);
		assertThat(director.getName()).isEqualTo("Кристофер Нолан");
	}

	// Создание режиссёра и проверка наличия его в БД
	@Test
	void testAddDirector() {
		Director newDirector = new Director();
		newDirector.setName("Мартин Скорсезе");

		Director createdDirector = directorStorage.addDirector(newDirector);

		assertThat(createdDirector).isNotNull();
		assertThat(createdDirector.getId()).isNotNull();
		assertThat(createdDirector.getName()).isEqualTo("Мартин Скорсезе");

		Director fromDbDirector = directorStorage.getDirectorById(createdDirector.getId());
		assertThat(fromDbDirector).isNotNull();
		assertThat(fromDbDirector.getName()).isEqualTo("Мартин Скорсезе");
	}

	// Обновление данных режиссёра и проверка обновления в БД
	@Test
	void testUpdateDirector() {
		Director directorToUpdate = new Director();
		directorToUpdate.setId(1L);
		directorToUpdate.setName("Фёдор Бондарчук");

		Director updatedDirector = directorStorage.updateDirector(directorToUpdate);

		assertThat(updatedDirector).isNotNull();
		assertThat(updatedDirector.getId()).isEqualTo(1L);
		assertThat(updatedDirector.getName()).isEqualTo("Фёдор Бондарчук");

		Director fromDbDirector = directorStorage.getDirectorById(1L);
		assertThat(fromDbDirector).isNotNull();
		assertThat(fromDbDirector.getName()).isEqualTo("Фёдор Бондарчук");
	}

	// Удаление режиссёра
	@Test
	public void testDeleteDirectorById() {
		Long directorId = 1L;

		directorStorage.deleteDirectorById(directorId);

		assertThrows(
				NotFoundException.class,
				() -> directorStorage.getDirectorById(directorId),
				"Ожидается, что после удаления режиссёра его получение по ID вызовет NotFoundException"
		);
	}
}