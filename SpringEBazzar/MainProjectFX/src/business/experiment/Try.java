package business.experiment;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;


public class Try {
	LocalDate d = LocalDate.now();
	public static void main(String[] args) {
		List<String> l = Arrays.asList("hi","bye");
		l = l.stream().map(s -> "hi").collect(Collectors.toList());
	}
	
}	
