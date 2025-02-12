package dk.jk.expenses.service;

import dk.jk.expenses.entity.Posting;
import dk.jk.expenses.entity.TreeNode;
import dk.jk.expenses.repository.PostingRepository;
import dk.jk.expenses.repository.TreeNodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileUploadService {

    private final PostingRepository postingRepository;
    private final TreeNodeRepository treeNodeRepository;
    private final TreeService treeService;

    public void processCsvFile(byte[] fileData) throws Exception {
        int offset = 0;
        int length = fileData.length;
        log.info("offset is {}, length is {}", offset, length);
        if (length > 3) {
            String firstThree = new String(Hex.encodeHex(fileData, 0, 3, true));
            log.info("First three bytes is {}", firstThree);
            if ("efbbbf".equalsIgnoreCase(firstThree)) {
                offset = 3;
                length = fileData.length - offset;
            }
        }
        log.info("offset is {}, length is {}", offset, length);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new java.io.ByteArrayInputStream(fileData, offset, length), StandardCharsets.UTF_8))) {

            Iterable<CSVRecord> records = CSVFormat.newFormat(';').builder()
                    .setHeader()
                    .setTrailingDelimiter(true)
                    .setSkipHeaderRecord(true)
                    .build()
                    .parse(reader);

            Map<Posting,Integer> postingCounter = new HashMap<>();

            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
            StreamSupport.stream(records.spliterator(), false)
                    .forEach(record -> {
                        Posting posting = new Posting();
                        //log.info(record.toString());
                        posting.setDate(LocalDate.parse(record.get("Bogforingsdato"), dateTimeFormatter));
                        posting.setText(record.get("Beskrivelse"));
                        posting.setAmount(new BigDecimal(record.get("Belob").replace(',','.')));
                        postingCounter.put(posting, postingCounter.getOrDefault(posting, 0) + 1);
                        posting.setSequenceNumber(postingCounter.get(posting));

                        // Build or find the path in the tree
                        String[] pathSegments = posting.getText().split("\\s+");
                        TreeNode finalNode = treeService.buildOrFindPath(pathSegments);
                        // Optionally, you can track the link between Posting and TreeNode
                        // if you want that relationship, but it's not strictly required by
                        // the original spec.

                        if (postingRepository.findByDateAndAmountAndTreeNodeAndSequenceNumber(posting.getDate(), posting.getAmount(), finalNode, posting.getSequenceNumber()).isEmpty()) {
                            posting.setTreeNode(finalNode);
                            postingRepository.save(posting);
                        }
                    });
        }
    }
}
