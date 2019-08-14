package shadow.sms_manager;

/**
 * Created by Peyman Razi on 24/02/2019.
 */

public class News {
    public int image;
    public String name, phone,id;
    public boolean check;

    public News(int img,String phone,String id,String name,boolean check)
    {
        this.check=check;
        this.image=img;
        this.name=name;
        this.phone=phone;
        this.id=id;
    }

}
