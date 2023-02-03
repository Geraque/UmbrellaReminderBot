package SpringDemoBot.service;

import SpringDemoBot.config.BotConfig;
import SpringDemoBot.exception.ResourceNotFoundException;
import SpringDemoBot.model.Info;
import SpringDemoBot.repository.InfoRepository;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
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
import java.util.regex.Pattern;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {


    @Autowired
    private InfoRepository infoRepository;

    final BotConfig config;

    String check;

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
                if(checkCreate(chatId)){
                    sendMessage(chatId,"Вы уже зарегистрировались, если хотите изменить параметры нажмите Изменить параметры!");
                }
                else{
                    sendMessage(chatId,"Введите название города и время через пробел");
                    check = "Регистрация";
                }
            }
            else if(messageText.equals("Изменить параметры")) {
                if(checkUpdate(chatId)){
                    check = "Изменить параметры";
                    sendMessage(chatId,"Введите название города и время через пробел");
                }
                else{
                    sendMessage(chatId,"Вы ещё не зарегистрировались, чтобы зарегистрировать нажмите Регистрация");
                }
            }
            else if(check.equals("Регистрация") && update.hasMessage() && update.getMessage().hasText()){
                System.out.println("Регистрация");
                if (!checkTime(messageText)){
                    sendMessage(chatId,"Введенно неправильно время, попробуйте снова");
                }
                else{
                    check=null;
                    createInfo(chatId, messageText);
                    sendMessage(chatId,"Успешная регистрация, для дальнейшей работы нажмите /start");
                }

            }
            else if(check.equals("Изменить параметры") && update.hasMessage() && update.getMessage().hasText()){
                System.out.println("Изменить параметры");
                if (!checkTime(messageText)){
                    sendMessage(chatId,"Введенно неправильно время, попробуйте снова");
                }
                else{
                    check=null;
                    updateInfo(chatId, messageText);
                    sendMessage(chatId,"Данные успешно измененны, для дальнейшей работы нажмите /start");
                }
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

    private boolean checkCreate(long chatId){
        List<Info> listInfo = infoRepository.findAll();
        boolean check=false;
        for (Info i:listInfo) {
            if (i.getTelegramId() == chatId) {
                check = true;
                break;
            }
        }
        return check;
    }

    private boolean checkUpdate(long chatId){
        List<Info> listInfo = infoRepository.findAll();
        boolean check=false;
        for (Info i:listInfo) {
            if (i.getTelegramId() == chatId) {
                check = true;
                break;
            }
        }
        return check;
    }

    private boolean checkTime(String messageText){
        String[] arr = messageText.split(" ");
        return Pattern.matches("([01]\\d|2[0-3])(:[0-5]\\d)",arr[1]);
    }


    private void createInfo(long chatId, String messageText) {
        String[] arr = messageText.split(" ");
        Info info = new Info();
        info.setTelegramId(chatId);
        info.setCity(arr[0]);
        info.setTime(arr[1]);
        infoRepository.save(info);
    }

    private void updateInfo(long chatId, String messageText)  {
        List<Info> listInfo = infoRepository.findAll();
        String[] arr = messageText.split(" ");
        Info info = new Info(chatId,arr[0]);
        for (Info i:listInfo) {
            if(i.getTelegramId()==chatId){
                info = i;
                break;
            }
        }
        info.setTime(arr[1]);
        info.setCity(arr[0]);
        final Info updatedInfo = infoRepository.save(info);
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