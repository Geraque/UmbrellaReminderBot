package SpringDemoBot.service;

import SpringDemoBot.config.BotConfig;
import SpringDemoBot.model.Info;
import SpringDemoBot.repository.InfoRepository;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import javax.validation.Valid;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {


    @Autowired
    private InfoRepository infoRepository;

    final BotConfig config;

    String nuNice;

    public TelegramBot(BotConfig config){
        this.config = config;
        List<BotCommand> listOfCommands = new ArrayList<>();
        listOfCommands.add(new BotCommand("/start","Вернуться в главное меню"));
        try{
            this.execute(new SetMyCommands(listOfCommands,new BotCommandScopeDefault(),null));
        }
        catch (TelegramApiException e){
            log.error("Error occurred: "+e.getMessage() );
        }
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {

        if(update.hasMessage() && update.getMessage().hasText()){
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            if(messageText.equals("/start")){
                log.info("/start");
                sendMessage(chatId,"Для ввода информации нажмите на кнопку Зарегистрироваться");
            }
            else if(messageText.equals("Проверка")) {
                log.info("Проверка");
                checkWeather("Astrakhan");
            }
            else if(messageText.equals("Регистрация")) {
                if(update.hasMessage() && update.getMessage().hasText()){
                    messageText = update.getMessage().getText();
                    System.out.println(messageText+" SPESHEEEEEEEER");
                }
                nuNice = "Nice";
                sendMessage(chatId,"Введите название города и время через пробел");
            }
            else if(nuNice.equals("Nice") && update.hasMessage() && update.getMessage().hasText()){
                System.out.println("Я пашя сникерся");
                nuNice=null;
                createInfo(chatId, messageText);
                sendMessage(chatId,"Успешная регистрация, для дальнейшей работы нажмите /start");
            }
            else {
                sendMessage(chatId,"Такой функции нет!");
            }
        }
    }

    private String checkWeather(String city){
        RestTemplate restTemplate = new RestTemplate();

        String http = restTemplate.getForObject("https://api.openweathermap.org/data/2.5/weather?q="+city+"&appid=a947fd7f0a1a6759d0884765022b2146", String.class);
        http = http.replaceAll("[\\[-\\]\"]","");
        String[] arr = http.split(",");

        String answer1 = arr[3];
        StringBuilder answerb = new StringBuilder();
        for (int i = 5; i < answer1.length(); i++) {
            answerb.append(answer1.charAt(i));
        }
        String answer = String.valueOf(answerb);
        return  answer;
    }

    public Info createInfo(long chatId, String messageText) {
        String[] arr = messageText.split(" ");
        Info info = new Info();
        info.setTelegramId(chatId);
        info.setCity(arr[0]);
        info.setTime(arr[1]);
        return infoRepository.save(info);
    }


    private void startButton(SendMessage message){
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();

        List<KeyboardRow> keyboardRows = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        row.add("/start");
        row.add("Регистрация");
        row.add("Изменить параметры");

        keyboardRows.add(row);

        keyboardMarkup.setKeyboard(keyboardRows);
        message.setReplyMarkup(keyboardMarkup);
    }

    private void sendMessage(long chatId, String textToSend){
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);
        startButton(message);

        try{
            execute(message);
        }
        catch (TelegramApiException e){
            log.error("Error occurred: "+e.getMessage() );
        }
    }


    @Scheduled(cron = "0 0 0 * * *")
    private void SendAds(){
        var listInfo = infoRepository.findAll();

        Date dateNow = new Date();
        SimpleDateFormat formatForDateNow = new SimpleDateFormat("HH:mm");
        System.out.println("gnida");
        for (Info i: listInfo){
            if(formatForDateNow.format(dateNow).equals(i.getTime())){
                String weather = checkWeather(i.getCity());
                System.out.println(weather);
                if(weather.equals("Clouds") || weather.equals("Snow") || weather.equals("Rain")){
                    sendMessage(i.getTelegramId(),"Ты Слава Бэброу?");
                }
            }
            else {
                sendMessage(i.getTelegramId(),"Ты Слава Бэброу123123?");
            }
        }
    }
}