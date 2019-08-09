package com.core.model;

import java.util.ArrayList;
import java.util.List;

public class Hrorgs {
    public boolean success;
    public String master;
    public List<HrorgItem> hrorgs = new ArrayList<HrorgItem>();
    public List<Employee> employees = new ArrayList<Employee>();

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMaster() {
        return master;
    }

    public void setMaster(String master) {
        this.master = master;
    }

    public List<HrorgItem> getHrorgs() {
        return hrorgs;
    }

    public void setHrorgs(List<HrorgItem> hrorgs) {
        this.hrorgs = hrorgs;
    }

    public List<Employee> getEmployees() {
        return employees;
    }

    public void setEmployees(List<Employee> employees) {
        this.employees = employees;
    }

    public class HrorgItem {
        private String or_code;
        private String or_name;
        private int or_subof;
        private int or_isleaf;
        private int or_id;
        private int or_emcount;//员工人数

        public String getOr_code() {
            return or_code;
        }

        public void setOr_code(String or_code) {
            this.or_code = or_code;
        }

        public String getOr_name() {
            return or_name;
        }

        public void setOr_name(String or_name) {
            this.or_name = or_name;
        }

        public int getOr_subof() {
            return or_subof;
        }

        public void setOr_subof(int or_subof) {
            this.or_subof = or_subof;
        }

        public int getOr_isleaf() {
            return or_isleaf;
        }

        public void setOr_isleaf(int or_isleaf) {
            this.or_isleaf = or_isleaf;
        }

        public int getOr_id() {
            return or_id;
        }

        public void setOr_id(int or_id) {
            this.or_id = or_id;
        }

        public int getOr_emcount() {
            return or_emcount;
        }

        public void setOr_emcount(int or_emcount) {
            this.or_emcount = or_emcount;
        }
    }

    public class Employee {
        public String em_code;
        public int em_id;
        public String em_name;
        public String em_mobile;
        public String em_email;
        public String em_position;
        public String em_defaultorname;

        public int em_imid;

        public int getEm_imid() {
            return em_imid;
        }

        public void setEm_imid(int em_imid) {
            this.em_imid = em_imid;
        }

        public String getEm_code() {
            return em_code;
        }

        public void setEm_code(String em_code) {
            this.em_code = em_code;
        }


        public String getEm_mobile() {
            return em_mobile;
        }

        public void setEm_mobile(String em_mobile) {
            this.em_mobile = em_mobile;
        }

        public String getEm_email() {
            return em_email;
        }

        public void setEm_email(String em_email) {
            this.em_email = em_email;
        }

        public String getEm_position() {
            return em_position;
        }

        public void setEm_position(String em_position) {
            this.em_position = em_position;
        }

        public String getEm_defaultorname() {
            return em_defaultorname;
        }

        public void setEm_defaultorname(String em_defaultorname) {
            this.em_defaultorname = em_defaultorname;
        }

        public int getEm_id() {
            return em_id;
        }

        public void setEm_id(int em_id) {
            this.em_id = em_id;
        }

        public String getEm_name() {
            return em_name;
        }

        public void setEm_name(String em_name) {
            this.em_name = em_name;
        }

    }

}
