package org.dynamics.reader;

import org.dynamics.constant.Constant;
import org.dynamics.model.Categories;
import org.dynamics.model.FileImport;
import org.dynamics.model.Gender;
import org.dynamics.model.Person;
import org.dynamics.util.Utility;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class CsvFileReader extends Constant implements Reader<Person> {
    private FileReader reader;
    private FileImport fileImport;
    public CsvFileReader(String fileName,FileImport fileImport) throws FileNotFoundException {
        this.fileImport = fileImport;
        fileImport.setId(Utility.getRandom());
        fileImport.setFilePath(fileName);
        fileImport.setImportedBy(System.getProperty("user.name"));
        reader = new FileReader(fileName);
    }
    @Override
    public List<Person> read() throws IOException {
        Optional<String> line = Optional.empty();
        List<Person> persons = new LinkedList<>();
        try(BufferedReader br = new BufferedReader(reader)){
            String ln = "";
            while((ln = br.readLine())!=null){
                Person per = parse(ln.split(DELIMETER));
                if(per!=null)
                    persons.add(per);
            }
        }
        fileImport.setTotalCount(persons.size());
        fileImport.setPerson(persons);
        return persons;
    }

    public Person parse(String[] data){
        if(data[0].contains("name")){
            return null;
        }else{
            Person person = new Person();
            person.setId(Utility.getRandom());
            person.setName(data[0]);
            person.setGender(Gender.valueOf(data[1].toUpperCase()));
            person.setCategories(Categories.valueOf(data[2].toUpperCase()));
            person.setWeight(Double.valueOf(data[3]));
            person.setTeamName(data[4]);
            return person;
        }

    }
}
