import java.util.regex.Pattern;

public class RegexForBrendan
{
    public static void main (String [] args)
    {
        String input = "lol i pee green @jbiebsfan:";
        input = input.replaceAll("@\\p{Alnum}*:?", "");
        input = input.replaceAll("#\\p{Alnum}*", "");
        System.out.println(input);
   
            
            
    }
}