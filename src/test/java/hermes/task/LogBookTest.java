package hermes.task;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import hermes.HermesException;

/**
 * Tests {@link LogBook#listTasksDueBy(LocalDateTime)}.
 *
 * <p>That method is the only public one on LogBook that does not write to
 * storage, which makes it the natural unit to test: it is a pure query over the
 * tasks held in memory, so the same input always gives the same answer.
 */
public class LogBookTest {

    /** The moment every case measures against. Fixed, so the tests do not age. */
    private static final LocalDateTime CUTOFF = LocalDateTime.of(2026, 8, 30, 12, 0);

    /**
     * A directory JUnit creates and deletes per test. Constructing a LogBook
     * needs a Storage, and adding tasks writes through it, so the tests are
     * pointed at a throwaway file rather than the real data/Hermes.txt.
     */
    @TempDir
    Path tempDir;

    private LogBook logBook;

    @BeforeEach
    public void setUp() {
        logBook = new LogBook(new Storage(tempDir.resolve("Hermes.txt").toString()));
    }

    @Test
    public void listTasksDueBy_taskDueBeforeCutoff_taskListed() throws HermesException {
        logBook.log(new Deadline("essay", LocalDateTime.of(2026, 8, 29, 9, 0)));

        assertEquals("[D][ ] essay (by: 29 Aug 2026 0900)", logBook.listTasksDueBy(CUTOFF));
    }

    @Test
    public void listTasksDueBy_taskDueAfterCutoff_taskExcluded() throws HermesException {
        logBook.log(new Deadline("far future", LocalDateTime.of(2026, 12, 25, 9, 0)));

        assertEquals("Nothing is due by 30 Aug 2026 1200", logBook.listTasksDueBy(CUTOFF));
    }

    @Test
    public void listTasksDueBy_taskDueExactlyAtCutoff_taskListed() throws HermesException {
        // The cutoff is inclusive: a task due at the very moment asked about counts.
        logBook.log(new Deadline("on the dot", CUTOFF));

        assertEquals("[D][ ] on the dot (by: 30 Aug 2026 1200)", logBook.listTasksDueBy(CUTOFF));
    }

    @Test
    public void listTasksDueBy_completedTask_taskExcluded() throws HermesException {
        logBook.log(new Deadline("already done", LocalDateTime.of(2026, 8, 26, 9, 0)));
        logBook.mark(0);

        assertEquals("Nothing is due by 30 Aug 2026 1200", logBook.listTasksDueBy(CUTOFF));
    }

    @Test
    public void listTasksDueBy_todoWithoutDate_taskExcluded() throws HermesException {
        // A todo has no date, so it can never be due by anything.
        logBook.log(new ToDo("borrow book"));

        assertEquals("Nothing is due by 30 Aug 2026 1200", logBook.listTasksDueBy(CUTOFF));
    }

    @Test
    public void listTasksDueBy_severalTasksDue_listedSoonestFirst() throws HermesException {
        // Added out of order, and an event is measured by its start time.
        logBook.log(new Deadline("essay", LocalDateTime.of(2026, 8, 29, 9, 0)));
        logBook.log(new Deadline("old", LocalDateTime.of(2026, 8, 25, 8, 0)));
        logBook.log(new Event("meeting",
                LocalDateTime.of(2026, 8, 26, 10, 0), LocalDateTime.of(2026, 8, 26, 11, 0)));

        assertEquals("""
                [D][ ] old (by: 25 Aug 2026 0800)
                [E][ ] meeting (from: 26 Aug 2026 1000 to: 26 Aug 2026 1100)
                [D][ ] essay (by: 29 Aug 2026 0900)""",
                logBook.listTasksDueBy(CUTOFF));
    }

    @Test
    public void listTasksDueBy_emptyLogBook_noticeReturned() {
        assertEquals("Nothing is due by 30 Aug 2026 1200", logBook.listTasksDueBy(CUTOFF));
    }

    @Test
    public void listTasksDueBy_queryRun_listOrderUnchanged() throws HermesException {
        logBook.log(new Deadline("essay", LocalDateTime.of(2026, 8, 29, 9, 0)));
        logBook.log(new Deadline("old", LocalDateTime.of(2026, 8, 25, 8, 0)));

        logBook.listTasksDueBy(CUTOFF);

        // The query sorts inside its stream, so the numbering shown by list is
        // still insertion order afterwards.
        assertEquals("""
                1. [D][ ] essay (by: 29 Aug 2026 0900)
                2. [D][ ] old (by: 25 Aug 2026 0800)
                """, logBook.toString());
    }
}
