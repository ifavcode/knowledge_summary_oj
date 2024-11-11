package cn.guet.ksmvcmain.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class OJConfig {

    @Bean("defaultSetting")
    public Map<String,Object> defaultSetting(){
        Map<String,Object> map = new HashMap<>();
        map.put("maintenance_mode",false);
        map.put("enable_wait_result",true);
        map.put("enable_compiler_options",true);
        map.put("allowed_languages_for_compile_options",new ArrayList<String>());
        map.put("enable_command_line_arguments",true);
        map.put("enable_submission_delete",false);
        map.put("enable_callbacks",true);
        map.put("callbacks_max_tries",3);
        map.put("callbacks_timeout",5);
        map.put("enable_additional_files",true);
        map.put("max_queue_size",100);
        map.put("cpu_time_limit", 3);
        map.put("max_cpu_time_limit",10);
        map.put("cpu_extra_time",1);
        map.put("max_cpu_extra_time", 5);
        map.put("wall_time_limit",10);
        map.put("max_wall_time_limit",20);
        map.put("memory_limit",256000);
        map.put("max_memory_limit",512000);
        map.put("stack_limit",64000);
        map.put("max_stack_limit",128000);
        map.put("max_processes_and_or_threads",60);
        map.put("max_max_processes_and_or_threads",120);
        map.put("enable_per_process_and_thread_time_limit",false);
        map.put("allow_enable_per_process_and_thread_time_limit",true);
        map.put("enable_per_process_and_thread_memory_limit",false);
        map.put("allow_enable_per_process_and_thread_memory_limit",true);
        map.put("max_file_size",4096);
        map.put("max_max_file_size",40960);
        map.put("number_of_runs",1);
        map.put("max_number_of_runs",20);
        map.put("redirect_stderr_to_stdout",false);
        map.put("max_extract_size",10240);
        map.put("enable_batched_submissions",true);
        map.put("max_submission_batch_size",20);
        map.put("submission_cache_duration",1);
        map.put("use_docs_as_homepage",false);
        map.put("allow_enable_network",true);
        map.put("enable_network",false);
        return map;
    }
}
