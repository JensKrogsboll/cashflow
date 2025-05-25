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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PushbackInputStream;
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

    public void processCsvStream(InputStream inputStream) throws Exception {
        PushbackInputStream pushbackStream = new PushbackInputStream(inputStream, 3);
        byte[] bom = new byte[3];
        int read = pushbackStream.read(bom, 0, bom.length);

        boolean hasBom = read == 3 &&
                (bom[0] & 0xFF) == 0xEF &&
                (bom[1] & 0xFF) == 0xBB &&
                (bom[2] & 0xFF) == 0xBF;

        if (!hasBom && read > 0) {
            pushbackStream.unread(bom, 0, read);
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(pushbackStream, StandardCharsets.UTF_8))) {
            Iterable<CSVRecord> records = CSVFormat.newFormat(';').builder()
                    .setHeader()
                    .setTrailingDelimiter(true)
                    .setSkipHeaderRecord(true)
                    .build()
                    .parse(reader);

            Map<Posting, Integer> postingCounter = new HashMap<>();
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");

            for (CSVRecord record : records) {
                Posting posting = new Posting();
                posting.setDate(LocalDate.parse(record.get("Bogforingsdato"), dateTimeFormatter));
                posting.setText(record.get("Beskrivelse"));
                posting.setAmount(new BigDecimal(record.get("Belob").replace(',', '.')));

                postingCounter.put(posting, postingCounter.getOrDefault(posting, 0) + 1);
                posting.setSequenceNumber(postingCounter.get(posting));

                String[] pathSegments = posting.getText().split("\\s+");
                TreeNode finalNode = treeService.buildOrFindPath(pathSegments);

                if (postingRepository.findByDateAndAmountAndTreeNodeAndSequenceNumber(
                        posting.getDate(), posting.getAmount(), finalNode, posting.getSequenceNumber()).isEmpty()) {

                    posting.setTreeNode(finalNode);
                    postingRepository.save(posting);
                }
            }
        }
    }

}
