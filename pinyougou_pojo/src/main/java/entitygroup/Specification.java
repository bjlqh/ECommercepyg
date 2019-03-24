package entitygroup;

import com.pinyougou.pojo.TbSpecification;
import com.pinyougou.pojo.TbSpecificationOption;

import java.io.Serializable;
import java.util.List;

public class Specification implements Serializable {

    private TbSpecification tbSpecification;

    private List<TbSpecificationOption> options;

    public TbSpecification getTbSpecification() {
        return tbSpecification;
    }

    public void setTbSpecification(TbSpecification tbSpecification) {
        this.tbSpecification = tbSpecification;
    }

    public List<TbSpecificationOption> getOptions() {
        return options;
    }

    public void setOptions(List<TbSpecificationOption> options) {
        this.options = options;
    }
}
