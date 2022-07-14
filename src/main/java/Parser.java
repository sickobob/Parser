import com.microsoft.playwright.*;
import com.microsoft.playwright.options.WaitUntilState;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.math.*;
import javax.management.timer.TimerNotification;
import java.util.Arrays;
import java.util.Scanner;
import java.util.stream.*;

public class Parser {
    public static void main(String[] args) {


        Scanner in = new Scanner(System.in);

        System.out.print("Input key words: ");
        String keyWord = "&text=" + in.nextLine();
        System.out.print("Input region id: ");
        String regionId = "area=" + in.nextLine();
        System.out.print("Input work experience: ");
        String s = in.nextLine();
        String workExp = "";
        switch (s) {
            case "i":
                workExp = "";
                break;
            case "iii":
                workExp = "&experience=between1And3";
                break;
            case "ii":
                workExp = "&experience=noExperience";
                break;
            case "iv":
                workExp = "&experience=between3And6";
                break;
            case "v":
                workExp = "&experience=moreThan6";
                break;
            default:
                System.out.println("work exp is none");

        }
        //считывание параметров конфигурации


        in.close();
        String url = "https://hh.ru/search/vacancy?{areaIS}{workExp}&search_field=name&search_field=company_name&search_field=description{keywordIS}&clusters=true&ored_clusters=true&enable_snippets=true".replace("{keywordIS}", keyWord).replace("{workExp}", workExp).replace("{areaIS}", regionId).replace("\\s", "");
        System.out.println(url);
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
            Page page = browser.newPage();
            page.navigate(url, new Page.NavigateOptions()
                    .setWaitUntil(WaitUntilState.DOMCONTENTLOADED));//запуская хромиум через либу playwright
            int n = 0; //счетчик количества полей, в которых есть данные(числовые---->которые можно спарсить)
            while (page.innerText("div.novafilters >div:nth-child(4)>div.novafilters-group-wrapper>div.novafilters-group__items>li:nth-child({i})>label>span>span:nth-child(1)".replace("{i}", Integer.toString(n + 2))) != "Своя зарплата") {
                try {
                    Integer.parseInt(page.innerText("div.novafilters-group__items :nth-child({i}) > label.bloko-radio>span :nth-child(1)".replace("{i}", Integer.toString(n + 2))).substring(3, 10).trim().replace(" ", ""));
                    n++;
                } catch (Exception ex) {
                    System.out.println(ex.getMessage());
                    break;
                }
                //блок try catch использую бля отсновки процесса, т к кроме числовых данных есть еще и текст
            }
            //  System.out.println(n);
            int[] nums = new int[n + 1]; //массив с кол-вом вакансий
            double[] sums = new double[n + 1]; //массив с зп


            for (int i = 0; i < nums.length - 1; i++) {

                nums[i] = Integer.parseInt(page.innerText("div.novafilters >div:nth-child(4)>div.novafilters-group-wrapper>div.novafilters-group__items>li:nth-child({i})>label>span>span:nth-child(2)".replace("{i}", Integer.toString(i + 2))));
                //    System.out.println(nums[i]);
            }
            //считывание с сайта


            for (int i = 0; i < sums.length - 1; i++) {

                sums[i] = Integer.parseInt(page.innerText("div.novafilters >div:nth-child(4)>div.novafilters-group-wrapper>div.novafilters-group__items>li:nth-child({i})>label>span>span:nth-child(1)".replace("{i}", Integer.toString(i + 2))).substring(3, 10).trim().replace(" ", ""));
                //    System.out.println(sums[i]);
            }
            //считывание с сайта
            sums[n] = sums[n - 1] * 1.2; //послений элемент вычисляется по формуле
            System.out.println(GetValue(nums, sums, n));
        } catch (Exception ex) {
            System.out.println("retry");
        }
    }

    public static double GetValue(int[] nums, double[] sums, int n) {

        double[] sredZ = new double[n];//массив со сред зп
        int[] counts = new int[n];//массив столбца E
        double[] fMas = new double[n];//массив столбца F
        for (int i = 0; i < sums.length - 1; i++) {
            sredZ[i] = (sums[i] + sums[i + 1]) / 2;
        }
        for (int i = 0; i < nums.length - 1; i++) {
            counts[i] = nums[i] - nums[i + 1];
        }
        for (int i = 0; i < nums.length - 1; i++) {
            fMas[i] = sums[i] * counts[i];
        }
        return DoubleStream.of(fMas).sum() / IntStream.of(counts).sum();
    }

}
