import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    // Для создания нужно три текстовых файла с именами new.txt  new_1.txt  Plan.txt
    static int pittB80;
    static int pittB95;
    static ArrayList<String> mainList = new ArrayList<>();
    static ArrayList<String> mainSupport = new ArrayList<>();
    static int countDate = 0;

    public static void main(String[] args) throws IOException, ParseException {
        origon();

        Pattern patternName = Pattern.compile("\\d{0,3}? - [a-zA-Z]+?\\s[a-zA-Z]+?"); // для поиска имени  10 - Janek Reemann
        Pattern patternReplay = Pattern.compile("([BD])\\d{0,2}([RL])/"); // для удаления лишнего B90R/
        Pattern patternIns = Pattern.compile("(INSPIRA)"); // для нахождения INSPIRA и укорачивания до INS
        Pattern patternExtra = Pattern.compile("1R/|(?<=\\s)25/|(?<=\\s)3/|(?<=\\s)2/|(?<=\\s)15/|[AB]\\d{1,3}[RL]/|35/|02[LR]/");
        Pattern patternProto = Pattern.compile("(PROTO PROTO kombinatsioon 1tk;)");
        Pattern patternDate = Pattern.compile("^\\d{1,2}[.]\\d{1,2}[.]\\d{4}");
        Matcher m_nameSearching = null;
        Matcher m_B90R_L_Searching = null;
        Matcher m_INSPIRA_Searching = null;
        Matcher m_1R_25_3_2_15_serching = null;
        Matcher m_Proto_searching = null;
        Matcher m_date_Searshing = null;


        String day = "";
        String name = "";
        String line;
        int forDate = 0;
        System.out.println("Очередная проверка "+ mainSupport.size() + "строк" );
        for (int i = 0; i < mainSupport.size(); i++) {
            line = mainSupport.get(i);
            // Забиваем переменные на совпадения

            m_nameSearching = patternName.matcher(line.trim()); // c помощью trim убераем пробелы с переди и с зади.
            m_B90R_L_Searching = patternReplay.matcher(line.trim());
            m_INSPIRA_Searching = patternIns.matcher(line.trim());
            m_1R_25_3_2_15_serching = patternExtra.matcher(line.trim());
            m_Proto_searching = patternProto.matcher(line.trim());
            m_date_Searshing = patternDate.matcher(line.trim());

            if (m_date_Searshing.find() ){
                day = daysDeterminate(line);        // находим дату и формируем её в удобную форму
            }else if (m_nameSearching.find()) {     // ищем Имена
                name = namesDeterminate(line);
            } else {                                 //Работа с регуляками
                line = workWithRegularExpresions(line, m_B90R_L_Searching, m_INSPIRA_Searching, m_1R_25_3_2_15_serching, m_Proto_searching);

                //Удаляем пробелы и подготавливаем строку к записи

                line = deletingExtraSpaces(line.substring(2)); // поддержка со 2 символа, что бы весь номер не выводить.

                line = name + " " + line + " | | " + day;
                mainList.add(line);
            }
        }
        System.out.println("Переработка в main формат "+ mainList.size() + "строк" );


        mainList.add("NB----->>> Padi Pitt/uma/living/Lux A80 - " + pittB80 + " tk " + "; A95 - " + pittB95 + " tk "); // добавляю количество люксовых подушек


        String s = mainList.get(0);
        if (s.contains("TOOTMISPLAAN")){
            s = s.substring(18,s.length()-4);
            mainList.remove(0);
            mainList.add(0,s);
        }


        File file = new File("C:\\Users\\Nordis\\Desktop\\Plan.txt");
        FileWriter fileWriter = new FileWriter(file);
        BufferedWriter bf = new BufferedWriter(fileWriter);
        bf.flush();

        for (int i = 0; i < mainList.size(); i++) {
            bf.write(mainList.get(i));
            bf.newLine();
        }

        bf.close();
        fileWriter.close();
        writeOfexist();
    }

    public static void origon() throws IOException {
        Pattern p = Pattern.compile("[a-zA-Z]");
        String ln = null;
        String CorrectString = null;
        String previousString = null;

        File file = new File("C:\\Users\\Nordis\\Desktop\\new.txt");
        FileReader fileReader = new FileReader(file);
        BufferedReader reader = new BufferedReader(fileReader);

        String line1;

        ArrayList<String> mainLoadList = new ArrayList<>(); // создаём новый аррай лист
        byte[] mybyte;
        while ((line1 = reader.readLine()) != null) {
            mybyte = line1.getBytes(StandardCharsets.UTF_8);
            // байт 9 это пробел который не пробел а символ.
            if (mybyte.length == 3 && mybyte[0]== 9 && mybyte[1]== 9 && mybyte[2]== 9){
                continue;
            }
            //System.out.println("добавляем "+ line1);
            mainLoadList.add(line1); // запихиваем прочитанное с файла в наш лист

        }
        System.out.println("Прочли файл, содержимое "+ mainLoadList.size() + " строк" );

        try {
            for (int i = 0; i < mainLoadList.size(); i++) {
                ln = mainLoadList.get(i);
                if (ln.substring(0, 2).contains("OR")) {   //Origon совмещаем Вариант 3
                    previousString = mainLoadList.get(i - 2);
                    String previos_number = previousString.substring(0, 8);
                    int oo = previousString.length();
                    String nnew = previos_number + ln + previousString.substring(15, oo);
                    mainLoadList.remove(i - 2);
                    mainLoadList.add(i - 2, nnew);
                    mainLoadList.remove(i);
                } else if (ln.substring(0, 1).matches(p.toString())) {
                    mainLoadList.remove(i);
                    mainLoadList.add(i, "        " + "1 " + ln);

                } else if (ln.contains("kokku")) {
                    mainLoadList.remove(i);
                } else if (ln.contains("MIAM")) {
                    String pp = ln.substring(18, ln.length()).replaceAll("(MIAM/)([NX])(/k-|/p)", ""); // уберает лишние MIAM/N/k-B85-H6R  1tk;  MIAM/N/k-QL  1tk
                    String lp = ln.substring(0, 17);
                    mainLoadList.remove(i);
                    mainLoadList.add(i, lp + pp);
                } else if (ln.contains("FOOTSTOOL") || ln.contains("Footstool")) {
                    String pp2 = ln.replaceAll("FOOTSTOOL|Footstool", "");
                    mainLoadList.remove(i);
                    mainLoadList.add(i, pp2);
                    i--;
                } else if (ln.contains("ALFA")) {
                    String pp = ln.substring(18, ln.length()).replaceAll("(ALF/)([NX])(/k-|/p)", ""); // уберает лишние ALF/N/k
                    String lp = ln.substring(0, 17);
                    mainLoadList.remove(i);
                    mainLoadList.add(i, lp + pp);
                } else if (ln.contains("PITTSBURGH/LUX") || ln.contains("UMA/LUX") || ln.contains("LIVINGSTON/LUX")) {
                    if (ln.contains("B80")) {
                        pittB80 += 2;
                    }
                    if (ln.contains("B95")) {
                        pittB95 += 2;
                    }
                }else if (ln.contains("F135")) {
                    CorrectString = ln + " { F104 }";
                    mainLoadList.remove(i);
                    mainLoadList.add(i, CorrectString);
                } else if (ln.contains("F132")) {
                    CorrectString = ln + " { F110 }";
                    mainLoadList.remove(i);
                    mainLoadList.add(i, CorrectString);
                } else if (ln.contains("IDAHO")) {
                    CorrectString = ln + " { ASPEN }";
                    mainLoadList.remove(i);
                    mainLoadList.add(i, CorrectString);
                } else if (ln.contains("FOREST")
                        //NATHALIE
                        || ln.contains("NICOL")
                        || ln.contains("BASEL")
                        || ln.contains("NATHALIE")) {
                    CorrectString = ln + " { LOUNGE }";
                    mainLoadList.remove(i);
                    mainLoadList.add(i, CorrectString);
                }else if (ln.contains("MICHELLE")) {
                    CorrectString = ln + " { kr - LOUNGE; kt - Nathalie }";
                    mainLoadList.remove(i);
                    mainLoadList.add(i, CorrectString);
                } else if (ln.contains("FEATHER")) {
                    CorrectString = ln + " {plan-kr: A95L/R-115; A118L/R-138; A95V-141; B75V-196}";
                    mainLoadList.remove(i);
                    mainLoadList.add(i, CorrectString);
                } else if (ln.contains("OREGON")) {
                    CorrectString = ln + " { LIFE }";
                    mainLoadList.remove(i);
                    mainLoadList.add(i, CorrectString);
                }else if (ln.contains("STOCKHOLM") || ln.contains("Stockholm")) {
                    CorrectString = ln + " { LOUNCH }";
                    mainLoadList.remove(i);
                    mainLoadList.add(i, CorrectString);
                }else if (ln.contains("JONAS") || ln.contains("Jonas")) {
                    CorrectString = ln + " { Valmont }";
                    mainLoadList.remove(i);
                    mainLoadList.add(i, CorrectString);
                }else if (ln.contains("F139")) {
                    CorrectString = ln + " { F129 }";
                    mainLoadList.remove(i);
                    mainLoadList.add(i, CorrectString);
                }else if (ln.contains("F141")) {
                    CorrectString = ln + " { F122 }";
                    mainLoadList.remove(i);
                    mainLoadList.add(i, CorrectString);
                }else if (ln.contains("F133")) {
                    CorrectString = ln + " { F111 }";
                    mainLoadList.remove(i);
                    mainLoadList.add(i, CorrectString);
                }else if (ln.contains("HUURRE")) {
                    CorrectString = ln + " { BOGART }";
                    mainLoadList.remove(i);
                    mainLoadList.add(i, CorrectString);
                }else if (ln.contains("F131")) {
                    CorrectString = ln + " { F122 }";
                    mainLoadList.remove(i);
                    mainLoadList.add(i, CorrectString);
                }else if (ln.contains("NASHVILLE B80V")) {
                    CorrectString = ln + " {Aero 80v= 25; 90v= 3}";
                    mainLoadList.remove(i);
                    mainLoadList.add(i, CorrectString);
                }else if (ln.contains("NASHVILLE B90V")) {
                    CorrectString = ln + " {Aero 80v= 25; 90v= 3}";
                    mainLoadList.remove(i);
                    mainLoadList.add(i, CorrectString);
                }else if (ln.contains("JACKSON")) {
                    CorrectString = ln + " {P = Doug}";
                    mainLoadList.remove(i);
                    mainLoadList.add(i, CorrectString);
                }else if (ln.contains("CLARA")) {
                    CorrectString = ln + " {P = Clara с ухом }";
                    mainLoadList.remove(i);
                    mainLoadList.add(i, CorrectString);
                }else if (ln.contains("EDITIONS")) {
                    CorrectString = ln + " {KR- pitt}";
                    mainLoadList.remove(i);
                    mainLoadList.add(i, CorrectString);
                }else if (ln.contains("SANTA FE")) {
                    CorrectString = ln + " {КТ 1H = Vanc}";
                    mainLoadList.remove(i);
                    mainLoadList.add(i, CorrectString);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        mainSupport.addAll(mainLoadList);
        System.out.println("Прошли первую проверку осталось "+ mainSupport.size()+ " строк" );
        reader.close();
        fileReader.close();
    }

    public static void writeOfexist() {
        System.out.println(mainList.size() + " Кол -во элементов " + "     Обчыно 420");
        System.out.println("Кол-во дaт " + countDate + ".           Неделя : 4 days(52)   5 days(65)  6 days(78)");
        System.out.println("Padi Pitt/uma/living/Lux B80 - " + pittB80 + "; B95 - " + pittB95);

    }
    private static String workWithRegularExpresions(String line,Matcher m_B90R_L_Searching,Matcher m_INSPIRA_Searching,Matcher m_1R_25_3_2_15_serching,Matcher m_Proto_searching1 ) {
        while (m_B90R_L_Searching.find()) {
            //System.out.println(line);
            line = line.replaceAll(m_B90R_L_Searching.group(), "");
        }
        if (m_INSPIRA_Searching.find()) {
            //System.out.println(line);
            line = line.replace(m_INSPIRA_Searching.group(), "INS");
        }
        while (m_1R_25_3_2_15_serching.find()) {
            //System.out.println(line);
            line = line.replace(m_1R_25_3_2_15_serching.group(), "");
        }
        if (m_Proto_searching1.find()){
            System.out.println("Come in");
            line =line.replace(m_Proto_searching1.group(), "");
        }



        return line;
    }

    private static String namesDeterminate(String line) {
        String name = "";
        if (line.contains("Oleksandr Kyselov")){
            name = "Oleks";
        }else{
            String[] splitName = line.split("\\s"); //с помощью split делим на массив
            name = splitName[2] + "." + splitName[3].charAt(0);
        }
        return name;
    }

    private static String daysDeterminate(String line) {
        countDate++;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        String dateString = line.substring(0, 10); // поддержка строки от 0 до 10
        LocalDate localDate = LocalDate.parse(dateString, formatter);
        String b = localDate.getDayOfWeek().toString();
        String day = "";
        if (b.contains("MONDAY")) {
            day = "Esm";
        } else if (b.contains("TUESDAY")) {
            day = "Tei";
        } else if (b.contains("WEDNESDAY")) {
            day = "Kol";
        } else if (b.contains("THURSDAY")) {
            day = "Nel";
        } else if (b.contains("FRIDAY")) {
            day = "Reede";
        } else if (b.contains("SATURDAY")) {
            day = "Lau";
        }

        return day;
    }

    private static String deletingExtraSpaces(String extraLine) {
        extraLine = extraLine.replaceAll("\\s+", " ");
        return extraLine;
    }
}

