package com.example.bot;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RecipeBot extends TelegramLongPollingBot {


    private final String BOT_USERNAME = "@Chef_Foxy_bot";
    private final String BOT_TOKEN = System.getenv("BOT_TOKEN");

    private final Map<Long, UserState> userStates = new ConcurrentHashMap<>();
    private final Map<String, List<String>> ingredientHierarchy = new HashMap<>();
    private final Map<String, Recipe> recipes = new HashMap<>();

    private static class UserState {
        String currentCategory;
        List<String> selectedIngredients = new ArrayList<>();
        int currentStep = -1;
        boolean isCooking = false;
    }

    private static class Recipe {
        String name;
        String imageUrl;
        List<String> ingredients;
        List<String> steps;
        int cookingTime; // В минутах
        String recommendedDrink;

        Recipe(String name, String imageUrl, List<String> ingredients, List<String> steps, int cookingTime, String recommendedDrink) {
            this.name = name;
            this.imageUrl = imageUrl;
            this.ingredients = ingredients;
            this.steps = steps;
            this.cookingTime = cookingTime;
            this.recommendedDrink = recommendedDrink;
        }
    }

    public RecipeBot() {
        initializeIngredients();
        initializeRecipes();
    }

    private void initializeIngredients() {
        ingredientHierarchy.put("Мясо", Arrays.asList("Свинина", "Говядина", "Курица", "Индейка", "Рыба", "Назад"));
        ingredientHierarchy.put("Свинина", Arrays.asList("Шейка", "Карбонад", "Лопатка", "Назад"));
        ingredientHierarchy.put("Говядина", Arrays.asList("Вырезка", "Ребро", "Филе", "Назад"));
        ingredientHierarchy.put("Курица", Arrays.asList("Грудка", "Бедро", "Крылья", "Назад"));
        ingredientHierarchy.put("Индейка", Arrays.asList("Филе", "Голень", "Назад"));
        ingredientHierarchy.put("Рыба", Arrays.asList("Лосось", "Треска", "Назад"));
        ingredientHierarchy.put("Гарнир", Arrays.asList("Картофель", "Рис", "Гречневая лапша", "Паста", "Назад"));
        ingredientHierarchy.put("Соус", Arrays.asList("Томатный", "Сливочный", "Терияки", "Песто", "Назад"));
        ingredientHierarchy.put("Напиток", Arrays.asList("Вино", "Пиво", "Джин-тоник", "Вода", "Пропустить", "Назад"));
        ingredientHierarchy.put("Вино", Arrays.asList("Красное сухое", "Красное полусладкое", "Белое сухое", "Белое полусладкое", "Назад"));
        ingredientHierarchy.put("Пиво", Arrays.asList("Лагер", "Эль", "Стаут", "Назад"));
    }

    private void initializeRecipes() {
        // Свинина
        recipes.put("Шейка+Картофель+Томатный", new Recipe(
                "Свинина с картофелем в томатном соусе",
                "https://example.com/pork_potato.jpg",
                Arrays.asList("Свиная шейка", "Картофель", "Томатный соус"),
                Arrays.asList(
                        "Нарежьте картофель кубиками.",
                        "Варите картофель в подсоленной воде 10 минут.",
                        "Нарежьте свиную шейку кубиками.",
                        "Обжарьте мясо на среднем огне 7 минут.",
                        "Добавьте вареный картофель к мясу.",
                        "Залейте томатным соусом, тушите 25 минут."
                ),
                45,
                "Красное сухое"
        ));
        recipes.put("Карбонад+Рис+Сливочный", new Recipe(
                "Свинина с рисом в сливочном соусе",
                "https://example.com/pork_rice.jpg",
                Arrays.asList("Свиной карбонад", "Рис", "Сливочный соус"),
                Arrays.asList(
                        "Промойте рис, варите в подсоленной воде 15 минут.",
                        "Нарежьте карбонад ломтиками.",
                        "Обжарьте мясо на среднем огне 5 минут.",
                        "Добавьте вареный рис к мясу.",
                        "Залейте сливочным соусом, тушите 20 минут."
                ),
                45,
                "Белое полусладкое"
        ));
        recipes.put("Лопатка+Гречневая лапша+Терияки", new Recipe(
                "Свинина вок с гречневой лапшой",
                "https://example.com/pork_wok.jpg",
                Arrays.asList("Свиная лопатка", "Гречневая лапша", "Соус терияки"),
                Arrays.asList(
                        "Варите гречневую лапшу в подсоленной воде 5 минут.",
                        "Нарежьте лопатку полосками.",
                        "Обжарьте мясо на сильном огне 6 минут.",
                        "Добавьте вареную лапшу, залейте соусом терияки.",
                        "Тушите 8 минут, помешивая."
                ),
                25,
                "Лагер"
        ));
        recipes.put("Шейка+Паста+Песто", new Recipe(
                "Свинина с пастой и соусом песто",
                "https://example.com/pork_pasta.jpg",
                Arrays.asList("Свиная шейка", "Паста", "Соус песто"),
                Arrays.asList(
                        "Варите пасту в подсоленной воде до аль денте 8 минут.",
                        "Нарежьте свиную шейку кубиками.",
                        "Обжарьте мясо на среднем огне 7 минут.",
                        "Смешайте пасту с мясом.",
                        "Добавьте соус песто, перемешайте, прогрейте 2 минуты."
                ),
                30,
                "Белое сухое"
        ));

        // Говядина
        recipes.put("Вырезка+Картофель+Сливочный", new Recipe(
                "Говядина с картофелем в сливочном соусе",
                "https://example.com/beef_potato.jpg",
                Arrays.asList("Говяжья вырезка", "Картофель", "Сливочный соус"),
                Arrays.asList(
                        "Нарежьте картофель кубиками.",
                        "Варите картофель в подсоленной воде 10 минут.",
                        "Нарежьте вырезку ломтиками.",
                        "Обжарьте мясо на среднем огне 6 минут.",
                        "Добавьте вареный картофель.",
                        "Залейте сливочным соусом, тушите 30 минут."
                ),
                50,
                "Красное сухое"
        ));
        recipes.put("Ребро+Рис+Томатный", new Recipe(
                "Говяжьи ребра с рисом",
                "https://example.com/beef_ribs.jpg",
                Arrays.asList("Говяжьи ребра", "Рис", "Томатный соус"),
                Arrays.asList(
                        "Промойте рис, варите в подсоленной воде 15 минут.",
                        "Обжарьте ребра на среднем огне 10 минут.",
                        "Добавьте вареный рис к ребрам.",
                        "Залейте томатным соусом, тушите под крышкой 40 минут."
                ),
                65,
                "Стаут"
        ));
        recipes.put("Филе+Гречневая лапша+Терияки", new Recipe(
                "Говядина вок с гречневой лапшой",
                "https://example.com/beef_wok.jpg",
                Arrays.asList("Говяжье филе", "Гречневая лапша", "Соус терияки"),
                Arrays.asList(
                        "Варите гречневую лапшу в подсоленной воде 5 минут.",
                        "Нарежьте филе полосками.",
                        "Обжарьте мясо на сильном огне 5 минут.",
                        "Добавьте вареную лапшу, залейте соусом терияки.",
                        "Тушите 7 минут, помешивая."
                ),
                25,
                "Эль"
        ));
        recipes.put("Вырезка+Паста+Песто", new Recipe(
                "Говядина с пастой и соусом песто",
                "https://example.com/beef_pasta.jpg",
                Arrays.asList("Говяжья вырезка", "Паста", "Соус песто"),
                Arrays.asList(
                        "Варите пасту в подсоленной воде до аль денте 8 минут.",
                        "Нарежьте вырезку кубиками.",
                        "Обжарьте мясо на среднем огне 6 минут.",
                        "Смешайте пасту с мясом.",
                        "Добавьте соус песто, перемешайте, прогрейте 2 минуты."
                ),
                30,
                "Красное полусладкое"
        ));

        // Курица
        recipes.put("Бедро+Гречневая лапша+Терияки", new Recipe(
                "Вок с курицей",
                "https://example.com/chicken_wok.jpg",
                Arrays.asList("Куриное бедро", "Гречневая лапша", "Соус терияки"),
                Arrays.asList(
                        "Варите гречневую лапшу в подсоленной воде 5 минут.",
                        "Нарежьте куриное бедро полосками.",
                        "Обжарьте курицу на сильном огне 5 минут.",
                        "Добавьте вареную лапшу, залейте соусом терияки.",
                        "Тушите 7 минут, помешивая."
                ),
                20,
                "Джин-тоник"
        ));
        recipes.put("Грудка+Рис+Сливочный", new Recipe(
                "Курица с рисом в сливочном соусе",
                "https://example.com/chicken_rice.jpg",
                Arrays.asList("Куриная грудка", "Рис", "Сливочный соус"),
                Arrays.asList(
                        "Промойте рис, варите в подсоленной воде 15 минут.",
                        "Нарежьте грудку кубиками.",
                        "Обжарьте курицу на среднем огне 5 минут.",
                        "Добавьте вареный рис к курице.",
                        "Залейте сливочным соусом, тушите 20 минут."
                ),
                45,
                "Белое сухое"
        ));
        recipes.put("Крылья+Картофель+Томатный", new Recipe(
                "Куриные крылья с картофелем",
                "https://example.com/chicken_wings.jpg",
                Arrays.asList("Куриные крылья", "Картофель", "Томатный соус"),
                Arrays.asList(
                        "Нарежьте картофель кубиками.",
                        "Варите картофель в подсоленной воде 10 минут.",
                        "Обжарьте крылья на среднем огне 8 минут.",
                        "Добавьте вареный картофель к крыльям.",
                        "Залейте томатным соусом, тушите 25 минут."
                ),
                45,
                "Лагер"
        ));
        recipes.put("Бедро+Паста+Песто", new Recipe(
                "Курица с пастой и соусом песто",
                "https://example.com/chicken_pasta.jpg",
                Arrays.asList("Куриное бедро", "Паста", "Соус песто"),
                Arrays.asList(
                        "Варите пасту в подсоленной воде до аль денте 8 минут.",
                        "Нарежьте бедро кубиками.",
                        "Обжарьте курицу на среднем огне 6 минут.",
                        "Смешайте пасту с мясом.",
                        "Добавьте соус песто, перемешайте, прогрейте 2 минуты."
                ),
                30,
                "Белое полусладкое"
        ));

        // Индейка
        recipes.put("Филе+Картофель+Сливочный", new Recipe(
                "Индейка с картофелем в сливочном соусе",
                "https://example.com/turkey_potato.jpg",
                Arrays.asList("Филе индейки", "Картофель", "Сливочный соус"),
                Arrays.asList(
                        "Нарежьте картофель кубиками.",
                        "Варите картофель в подсоленной воде 10 минут.",
                        "Нарежьте филе кубиками.",
                        "Обжарьте мясо на среднем огне 6 минут.",
                        "Добавьте вареный картофель.",
                        "Залейте сливочным соусом, тушите 25 минут."
                ),
                45,
                "Красное сухое"
        ));
        recipes.put("Голень+Рис+Томатный", new Recipe(
                "Индейка с рисом в томатном соусе",
                "https://example.com/turkey_rice.jpg",
                Arrays.asList("Голень индейки", "Рис", "Томатный соус"),
                Arrays.asList(
                        "Промойте рис, варите в подсоленной воде 15 минут.",
                        "Обжарьте голень на среднем огне 8 минут.",
                        "Добавьте вареный рис к мясу.",
                        "Залейте томатным соусом, тушите под крышкой 30 минут."
                ),
                55,
                "Стаут"
        ));
        recipes.put("Филе+Гречневая лапша+Терияки", new Recipe(
                "Индейка вок с гречневой лапшой",
                "https://example.com/turkey_wok.jpg",
                Arrays.asList("Филе индейки", "Гречневая лапша", "Соус терияки"),
                Arrays.asList(
                        "Варите гречневую лапшу в подсоленной воде 5 минут.",
                        "Нарежьте филе полосками.",
                        "Обжарьте мясо на сильном огне 5 минут.",
                        "Добавьте вареную лапшу, залейте соусом терияки.",
                        "Тушите 7 минут, помешивая."
                ),
                25,
                "Джин-тоник"
        ));
        recipes.put("Голень+Паста+Песто", new Recipe(
                "Индейка с пастой и соусом песто",
                "https://example.com/turkey_pasta.jpg",
                Arrays.asList("Голень индейки", "Паста", "Соус песто"),
                Arrays.asList(
                        "Варите пасту в подсоленной воде до аль денте 8 минут.",
                        "Нарежьте голень кубиками.",
                        "Обжарьте мясо на среднем огне 7 минут.",
                        "Смешайте пасту с мясом.",
                        "Добавьте соус песто, перемешайте, прогрейте 2 минуты."
                ),
                30,
                "Белое сухое"
        ));

        // Рыба
        recipes.put("Лосось+Рис+Сливочный", new Recipe(
                "Лосось с рисом в сливочном соусе",
                "https://example.com/salmon_rice.jpg",
                        Arrays.asList("Лосось", "Рис", "Сливочный соус"),
                        Arrays.asList(
                                "Промойте рис, варите в подсоленной воде 15 минут.",
                                "Нарежьте лосось кусками.",
                                "Обжарьте лосось 4 минуты с каждой стороны.",
                                "Добавьте вареный рис, залейте сливочным соусом.",
                                "Тушите 15 минут."
                        ),
                        40,
                        "Белое сухое"
                ));
        recipes.put("Треска+Картофель+Томатный", new Recipe(
                "Треска с картофелем в томатном соусе",
                "https://example.com/cod_potato.jpg",
                Arrays.asList("Треска", "Картофель", "Томатный соус"),
                Arrays.asList(
                        "Нарежьте картофель кубиками.",
                        "Варите картофель в подсоленной воде 10 минут.",
                        "Нарежьте треску кусками.",
                        "Обжарьте рыбу 5 минут.",
                        "Добавьте вареный картофель.",
                        "Залейте томатным соусом, тушите 20 минут."
                ),
                40,
                "Лагер"
        ));
        recipes.put("Лосось+Гречневая лапша+Терияки", new Recipe(
                "Лосось вок с гречневой лапшой",
                "https://example.com/salmon_wok.jpg",
                Arrays.asList("Лосось", "Гречневая лапша", "Соус терияки"),
                Arrays.asList(
                        "Варите гречневую лапшу в подсоленной воде 5 минут.",
                        "Нарежьте лосось полосками.",
                        "Обжарьте рыбу 4 минуты на сильном огне.",
                        "Добавьте вареную лапшу, залейте соусом терияки.",
                        "Тушите 6 минут, помешивая."
                ),
                20,
                "Джин-тоник"
        ));
        recipes.put("Треска+Паста+Песто", new Recipe(
                "Треска с пастой и соусом песто",
                "https://example.com/cod_pasta.jpg",
                Arrays.asList("Треска", "Паста", "Соус песто"),
                Arrays.asList(
                        "Варите пасту в подсоленной воде до аль денте 8 минут.",
                        "Нарежьте треску кусками.",
                        "Обжарьте рыбу 5 минут.",
                        "Смешайте пасту с рыбой.",
                        "Добавьте соус песто, перемешайте, прогрейте 2 минуты."
                ),
                25,
                "Белое полусладкое"
        ));
    }

    @Override
    public String getBotUsername() {
        return BOT_USERNAME;
    }

    @Override
    public String getBotToken() {
        return BOT_TOKEN;
    }
    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) return;

        long chatId = update.getMessage().getChatId();
        String messageText = update.getMessage().getText();
        UserState state = userStates.computeIfAbsent(chatId, k -> new UserState());

        try {
            if (messageText.equals("/start")) {
                state.currentCategory = null;
                state.selectedIngredients.clear();
                state.currentStep = -1;
                state.isCooking = false;
                sendWelcomeMessage(chatId);
            } else if (state.isCooking) {
                handleCookingProcess(chatId, messageText, state);
            } else {
                handleIngredientSelection(chatId, messageText, state);
            }
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendWelcomeMessage(long chatId) throws TelegramApiException {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Привет! Выбери категорию:");
        message.setReplyMarkup(getKeyboard(Arrays.asList("Мясо", "Гарнир", "Соус", "Напиток")));
        execute(message);
    }

    private void handleIngredientSelection(long chatId, String messageText, UserState state) throws TelegramApiException {
        if (ingredientHierarchy.containsKey(messageText)) {
            state.currentCategory = messageText;
            sendCategoryOptions(chatId, messageText);
        } else if (messageText.equals("Назад")) {
            state.currentCategory = getParentCategory(state.currentCategory);
            sendCategoryOptions(chatId, state.currentCategory);
        } else if (messageText.equals("Пропустить") && state.currentCategory.equals("Напиток")) {
            state.selectedIngredients.add("Без напитка");
            checkAndSendRecipe(chatId, state);
        } else if (isIngredient(messageText)) {
            state.selectedIngredients.add(messageText);
            checkAndSendRecipe(chatId, state);
        }
    }

    private void checkAndSendRecipe(long chatId, UserState state) throws TelegramApiException {
        String[] requiredCategories = {"Мясо", "Гарнир", "Соус", "Напиток"};
        int requiredCount = state.selectedIngredients.contains("Без напитка") ? 3 : 4;

        if (state.selectedIngredients.size() == requiredCount) {
            sendRecipe(chatId, state);
        } else {
            String nextCategory = requiredCategories[state.selectedIngredients.size()];
            sendCategoryOptions(chatId, nextCategory);
        }
    }

    private void sendCategoryOptions(long chatId, String category) throws TelegramApiException {
        if (category == null) {
            sendWelcomeMessage(chatId);
            return;
        }
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Выбери " + category.toLowerCase() + ":");
        message.setReplyMarkup(getKeyboard(ingredientHierarchy.get(category)));
        execute(message);
    }

    private boolean isIngredient(String text) {
        return !text.equals("Назад") && !Arrays.asList("Мясо", "Гарнир", "Соус", "Напиток", "Вино", "Пиво").contains(text) &&
                ingredientHierarchy.values().stream().anyMatch(list -> list.contains(text));
    }

    private String getParentCategory(String category) {
        if (category == null) return null;
        for (Map.Entry<String, List<String>> entry : ingredientHierarchy.entrySet()) {
            if (entry.getValue().contains(category)) {
                return entry.getKey();
            }
        }
        return null;
    }

    private void sendRecipe(long chatId, UserState state) throws TelegramApiException {
        String key = String.join("+", state.selectedIngredients.subList(0, Math.min(3, state.selectedIngredients.size())));
        Recipe recipe = recipes.getOrDefault(key, new Recipe(
                "Блюдо по вашим ингредиентам",
                "https://example.com/default.jpg",
                new ArrayList<>(state.selectedIngredients),
                Arrays.asList("Обжарьте мясо.", "Добавьте гарнир.", "Полейте соусом.", "Подавайте с напитком."),
                30,
                "Вода"
        ));

        String drink = state.selectedIngredients.size() > 3 ? state.selectedIngredients.get(3) : recipe.recommendedDrink;

        SendPhoto photo = new SendPhoto();
        photo.setChatId(chatId);
        photo.setPhoto(new InputFile(recipe.imageUrl));
        execute(photo);

        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Рецепт: " + recipe.name + "\nИнгредиенты: " + String.join(", ", recipe.ingredients) +
                "\nРекомендуемый напиток: " + drink + "\nВремя готовки: " + recipe.cookingTime + " минут" +
                "\n\nНачать готовить?");
        message.setReplyMarkup(getKeyboard(Arrays.asList("Начать готовить")));
        execute(message);
        state.isCooking = true;
    }

    private void handleCookingProcess(long chatId, String messageText, UserState state) throws TelegramApiException {
        String key = String.join("+", state.selectedIngredients.subList(0, Math.min(3, state.selectedIngredients.size())));
        Recipe recipe = recipes.getOrDefault(key, new Recipe(
                "Блюдо по вашим ингредиентам",
                "https://example.com/default.jpg",
                new ArrayList<>(state.selectedIngredients),
                Arrays.asList("Обжарьте мясо.", "Добавьте гарнир.", "Полейте соусом.", "Подавайте с напитком."),
                30,
                "Вода"
        ));

        if (messageText.equals("Начать готовить") || messageText.equals("Следующий шаг")) {
            state.currentStep++;
            if (state.currentStep < recipe.steps.size()) {
                sendStep(chatId, recipe.steps.get(state.currentStep), state.currentStep, recipe.steps.size());
            } else {
                sendCompletionMessage(chatId, state);
            }
        } else if (messageText.equals("Предыдущий шаг")) {
            state.currentStep = Math.max(-1, state.currentStep - 1);
            if (state.currentStep >= 0) {
                sendStep(chatId, recipe.steps.get(state.currentStep), state.currentStep, recipe.steps.size());
            } else {
                sendRecipe(chatId, state);
            }
        }
    }

    private void sendStep(long chatId, String step, int currentStep, int totalSteps) throws TelegramApiException {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Шаг " + (currentStep + 1) + "/" + totalSteps + ": " + step);
        List<String> buttons = new ArrayList<>(Arrays.asList("Следующий шаг"));
        if (currentStep > 0) buttons.add("Предыдущий шаг");
        message.setReplyMarkup(getKeyboard(buttons));
        execute(message);

        if (step.toLowerCase().contains("обжарьте") || step.toLowerCase().contains("тушите")) {
            SendMessage timerMessage = new SendMessage();
            timerMessage.setChatId(chatId);
            timerMessage.setText("Таймер: 5 минут (для примера)");
            execute(timerMessage);
        }
    }

    private void sendCompletionMessage(long chatId, UserState state) throws TelegramApiException {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Приятного аппетита!");
        message.setReplyMarkup(getKeyboard(Collections.emptyList()));
        execute(message);
        state.selectedIngredients.clear();
        state.currentStep = -1;
        state.isCooking = false;
        state.currentCategory = null;
    }

    private ReplyKeyboardMarkup getKeyboard(List<String> buttons) {
        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
        keyboard.setResizeKeyboard(true);
        List<KeyboardRow> rows = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        for (int i = 0; i < buttons.size(); i++) {
            if (i > 0 && i % 2 == 0) {
                rows.add(row);
                row = new KeyboardRow();
            }
            row.add(buttons.get(i));
        }
        if (!row.isEmpty()) rows.add(row);
        keyboard.setKeyboard(rows);
        return keyboard;
    }
}