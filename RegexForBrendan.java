import java.util.regex.Pattern;

public class RegexForBrendan
{
    public static void main (String [] args)
    {
        String input = "lol i pee green @jbiebsfan:";
        input = input.replaceAll("@\\p{Alpha}*:?","");
        input = input.replaceAll("#\\p{Alpha}*", "");
        System.out.println(input);
   
            
            
    }
}