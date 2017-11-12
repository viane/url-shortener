/**
 * @param:
 *  long toBeConverted
 *  int LENGTH_OF_ORGINAL_URL
 * @apiNote
 *  Base62 without '-' and '_'
 *
 */

public class Base62Converter {

    private int LENGTH_OF_URL_CODE = 6;
    private int LENGTH_OF_ORGINAL_URL=0;
    private long baseID;

    public  Base62Converter(long id, String o_url){
        this.LENGTH_OF_ORGINAL_URL = o_url.length();
        this.baseID = id;
    }

    public void setHashLevel(int level){
        // level determine the hashed length
        // higher level => condensd hash url
        // variation depends on specific requirements
        switch (level) {
            case 1:
                this.LENGTH_OF_URL_CODE = 6;
                break;
            case 2:
                this.LENGTH_OF_URL_CODE = 6;
                break;
            default:
                this.LENGTH_OF_URL_CODE = 6;
        }
    }

    public String convertTo62Base()
    {
        String[] elements = {
                "a","b","c","d","e","f","g","h","i","j","k","l","m","n","o",
                "p","q","r","s","t","u","v","w","x","y","z","1","2","3","4",
                "5","6","7","8","9","0","A","B","C","D","E","F","G","H","I",
                "J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X",
                "Y","Z"
        };
        String convertedString="";
        int numOfDiffChars= elements.length;
        if(this.baseID<numOfDiffChars+1 && this.baseID>0)
        {
            convertedString=elements[(int) (this.baseID-1)];
        }
        else if(this.baseID>numOfDiffChars)
        {
            long mod = 0;
            long multiplier = 0;
            boolean determinedTheLength=false;
            for(int j=LENGTH_OF_URL_CODE;j>=0;j--)
            {
                multiplier=(long) (this.baseID/Math.pow(numOfDiffChars,j));
                if(multiplier>0 && this.baseID>=numOfDiffChars)
                {
                    convertedString+=elements[(int) multiplier];
                    determinedTheLength=true;
                }
                else if(determinedTheLength && multiplier==0)
                {
                    convertedString+=elements[0];
                }
                else if(this.baseID<numOfDiffChars)
                {
                    convertedString+=elements[(int) mod];
                }

                mod=(long) (this.baseID%Math.pow(numOfDiffChars,j));
                this.baseID=mod;
            }

        }
        return convertedString;
    }
}