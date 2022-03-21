package com.learning.texnar13.teachersprogect.settings.ImportModel;

import android.content.Context;
import android.net.Uri;

import com.learning.texnar13.teachersprogect.R;
import com.learning.texnar13.teachersprogect.data.DataBaseOpenHelper;
import com.learning.texnar13.teachersprogect.data.SchoolContract;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.FileInputStream;
import java.io.IOException;

public class SettingsImportHelper {// импорт данных


    public static ImportDataBaseData importDataBase(Context toContext, Uri selectedUriPath) {

        // обьект с прочитанными данными
        ImportDataBaseData returnData = new ImportDataBaseData(toContext.getResources().getText(R.string.settings_activity_data_import_message_error_tag).toString());

        // --- начинаем чтение из файла ---
        FileInputStream inputStream = null;
        try {

            inputStream = (FileInputStream) toContext.getContentResolver().openInputStream(selectedUriPath);
            // пытаемся прочитать данные из файла
            parseDBFileFromStream(inputStream, returnData,
                    toContext.getResources().getStringArray(R.array.locale_code));

            returnData.addMessage(toContext.getResources().getText(
                    (returnData.criticalErrorFlag) ?
                            R.string.settings_activity_data_import_message_read_error :
                            R.string.settings_activity_data_import_message_read_success
            ).toString());


        } catch (IOException | XmlPullParserException e) {
            e.printStackTrace();
            // говорим, что приложение не может прочитать этот файл
            returnData.addError(toContext.getResources().getText(
                    R.string.settings_activity_data_import_message_unrtedable_file).toString());
            returnData.addError(e.toString());
        } finally {
            // закрываем поток чтения
            try {
                if (inputStream != null) inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        // --- Проверяем связи foreign key (внешние ключи) в считанных таблицах ---
        returnData.addMessage("### Считываю связи между полученными таблицами");
        returnData.checkForeignDependencies();

        returnData.addMessage(
                (returnData.criticalErrorFlag) ?
                        "###Ошибка, связи на основе импортируемого файла не построены" :
                        "###Успешно созданы связи на основе импортируемого файла"
        );



        // --- Пытаемся записать данные в тестовые таблицы ---
        DataBaseOpenHelper db = new DataBaseOpenHelper(toContext);

        db.testParsedData(returnData);

        db.close();







        // после чтения возвращем готовый обьект
        return returnData;
    }


    /**
     * Разбиваем поток на теги и читаем их с помощью parseTag
     */
    private static void parseDBFileFromStream(
            FileInputStream inputStream, ImportDataBaseData dataOutput, String[] localeCodes
    ) throws XmlPullParserException, IOException {

        // создаем обьект парсера и передаем ему поток для чтения
        XmlPullParser xpp;
        {
            XmlPullParserFactory xppf = XmlPullParserFactory.newInstance();
            xppf.setNamespaceAware(true);
            xpp = xppf.newPullParser();
            xpp.setInput(inputStream, null);
        }

        // проходимся по всем тегам
        int type = xpp.getEventType();
        while (type != XmlPullParser.END_DOCUMENT && !dataOutput.criticalErrorFlag) {
            // наступило событие "начало тега"
            if (type == XmlPullParser.START_TAG) {
                // парсим текущий тег
                parseTag(xpp, dataOutput, localeCodes);
            }
            // переходим к следующему "событию"
            type = xpp.next();
        }
    }

    // текущий тег
    private static String currentTable = null;

    /**
     * Пасим 1 тег из xml
     */
    private static void parseTag(XmlPullParser xpp, ImportDataBaseData output, String[] localeCodes) {

        String tagName = xpp.getName();
        switch (tagName) {

            // корневой тег файла с информацией о бд
            case SchoolContract.DB_NAME: {
                // проверяем был ли уже этот тег
                if (output.wasModelNotInitialized()) {
                    try {
                        // ищем в теге номер версии и передаем его модели бд
                        output.initializeModelVersion(xpp.getAttributeValue("", "db_file_version"), localeCodes);
                        // todo еще может быть такое что этот тег будет не первым,
                        //  и тогда ImportDataBaseData.dataBaseOutput будет null (но это уже потом, надо будет написать проверку на эту ошибку и текст для неё)
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                        output.addError("incorrect db_file_version");
                        output.criticalErrorFlag = true;
                    }
                } else {
                    output.addError("duplicate tag: " + SchoolContract.DB_NAME);
                    output.criticalErrorFlag = true;
                }
                break;
            }

            // разрешенные теги таблиц
            case SchoolContract.TableSettingsData.NAME_TABLE_SETTINGS:
            case SchoolContract.TableCabinets.NAME_TABLE_CABINETS:
            case SchoolContract.TableDesks.NAME_TABLE_DESKS:
            case SchoolContract.TablePlaces.NAME_TABLE_PLACES:
            case SchoolContract.TableClasses.NAME_TABLE_CLASSES:
            case SchoolContract.TableLearners.NAME_TABLE_LEARNERS:
            case SchoolContract.TableLearnersOnPlaces.NAME_TABLE_LEARNERS_ON_PLACES:
            case SchoolContract.TableLearnersGrades.NAME_TABLE_LEARNERS_GRADES: // todo там где пропуск, может храниться null, по этому надо будет добавить дополнительные проверки
            case SchoolContract.TableSubjects.NAME_TABLE_SUBJECTS:
            case SchoolContract.TableSubjectAndTimeCabinetAttitude.NAME_TABLE_SUBJECT_AND_TIME_CABINET_ATTITUDE:
            case SchoolContract.TableStatisticsProfiles.NAME_TABLE_STATISTICS_PROFILES:
            case SchoolContract.TableLearnersGradesTitles.NAME_TABLE_LEARNERS_GRADES_TITLES:
            case SchoolContract.TableLearnersAbsentTypes.NAME_TABLE_LEARNERS_ABSENT_TYPES:
            case SchoolContract.TableLessonComment.NAME_TABLE_LESSON_TEXT: {
                // устанавливаем текущую таблицу для записи в неё "element"
                currentTable = tagName;
                break;
            }

            // тег с одной записью таблицы
            case "element": {

                /* В ImportModel Все реализовано так:
                 *  На вход метода check() подается переменная преобразованная из строки в тип, который указан в колонке.
                 *  По итогу если это int, то в int оно уже было переведено, а затем упаковано в Object.
                 *  Внутри check() проверяется границы в пределах которых должно находиться значение (кстати todo в строках надо бы проверять длинну)
                 *  check() возвращает boolean, исключения он не кидает, так как на вход идут данные уже нужного типа
                 *
                 *  Скорее всего стоит переделать эту схему, и проверки типов переместить уже в ImportModel, передавая в check() String
                 *
                 *
                 * Или второй вариант, все передается в addRow(Object[] data)
                 *  (data здесь содержит обьекты уже проверенные и преобразованные из строки в тип),
                 *  и там уже для каждого поля вызывается проверка check()
                 *
                 *  Этот способ можно переделать так:
                 *  - У нужной таблицы (TableModel) вызывать addRow(todo String[] data, ImportDataBaseData output).
                 *  - Затем в addRow, переданные строки проверяются по типу, через try catch.
                 *  - Проверенные строки проверяются уже непосредственно через проверки описания бд,
                 *   который все еще будет возвращать boolean и принимать object
                 *
                 *  Необходимо навести порядок в ImportModel:
                 *   Все проверки надо перенести в одно место друг за другом, даже те, которые используются 1 раз.
                 *   Надо разделить этот класс на части сменяющиеся и не меняющиеся между обновлениями.
                 *   То есть, к примеру TableModel меняться не будет, а часть с конструктором ImportModel_v1() будет.
                 *
                 *   Теоретически можно даже сделать абстрактный класс а может и нет, плохая идея (специально оставил)
                 *
                 */


                // проверяем распознается ли сейчас какая-нибудь таблица
                if (currentTable == null) {
                    output.addError("tag \"element\" outside any table");
                } else {
                    // получаем текущую таблицу из модели
                    TableModel currentTableModel = output.getTableModelByName(currentTable);

                    // получаем массив значений из тега
                    String[] rawData = new String[currentTableModel.tableHead.length];

                    // проходимся по всем полям записи и записываем их в массив проверяя на null
                    boolean errorFlag = false;
                    for (int columnI = 0; columnI < currentTableModel.tableHead.length && !errorFlag; columnI++) {

                        // получаем значение поля по названию
                        rawData[columnI] = xpp.getAttributeValue("",
                                currentTableModel.tableHead[columnI].getFieldDBName());

                        // сразу отбрасываем пустые поля
                        if (rawData[columnI] == null) {
                            output.addError("(" + currentTable + ")tag \"element\" has empty "
                                    + currentTableModel.tableHead[columnI].getFieldDBName());
                            errorFlag = true;
                        }
                    }

                    // если все в порядке
                    if (!errorFlag)
                        try {
                            // пытаемся добавить строку
                            //  (передаем на вход считанные строки для преобразования в типы, а потом в данные)
                            currentTableModel.addRow(rawData);
                        } catch (IllegalArgumentException e) {
                            // выводим ошибку в лог
                            output.addError(e.getMessage());
                        }
                }
                break;
            }

            // другие теги не распознаются
            default:
                output.addError("incorrect tag: " + tagName);
                output.criticalErrorFlag = true;
        }
    }

}


/*
* Пример файла
*
* <?xml version='1.0' encoding='UTF-8' standalone='yes'?>
<school db_file_version="1">
	<settingsData>
		<element _id="1" profileName="simpleName" locale="defaultLocale" interfaceSize="null" maxAnswer="5" time='{"beginHour":["8","9","10","11","12","13","14","15","16"],"beginMinute":["30","30","30","30","25","30","25","20","6"],"endHour":["9","10","11","12","13","14","15","16","23"],"endMinute":["15","15","15","15","10","15","10","5","59"]}' areTheGradesColored="1"/>
	</settingsData>
	<cabinets>
		<element _id="1" sizeMultiplier="15" offsetX="0" offsetY="0" name="705"/>
	</cabinets>
	<desks>
		<element _id="1" x="160" y="420" numberOfPlaces="2" cabinetId="1"/>
		<element _id="2" x="260" y="420" numberOfPlaces="2" cabinetId="1"/>
		<element _id="3" x="160" y="480" numberOfPlaces="2" cabinetId="1"/>
		<element _id="4" x="260" y="480" numberOfPlaces="2" cabinetId="1"/>
		<element _id="5" x="160" y="540" numberOfPlaces="2" cabinetId="1"/>
		<element _id="6" x="260" y="540" numberOfPlaces="2" cabinetId="1"/>
		<element _id="7" x="360" y="540" numberOfPlaces="2" cabinetId="1"/>
		<element _id="8" x="360" y="480" numberOfPlaces="2" cabinetId="1"/>
		<element _id="9" x="360" y="420" numberOfPlaces="2" cabinetId="1"/>
	</desks>
	<places>
		<element _id="1" deskId="1" number="1"/>
		<element _id="2" deskId="1" number="2"/>
		<element _id="3" deskId="2" number="1"/>
		<element _id="4" deskId="2" number="2"/>
		<element _id="5" deskId="3" number="1"/>
		<element _id="6" deskId="3" number="2"/>
		<element _id="7" deskId="4" number="1"/>
		<element _id="8" deskId="4" number="2"/>
		<element _id="9" deskId="5" number="1"/>
		<element _id="10" deskId="5" number="2"/>
		<element _id="11" deskId="6" number="1"/>
		<element _id="12" deskId="6" number="2"/>
		<element _id="13" deskId="7" number="1"/>
		<element _id="14" deskId="7" number="2"/>
		<element _id="15" deskId="8" number="1"/>
		<element _id="16" deskId="8" number="2"/>
		<element _id="17" deskId="9" number="1"/>
		<element _id="18" deskId="9" number="2"/>
	</places>
	<classes>
		<element _id="2" className="7б"/>
	</classes>
	<learners>
		<element _id="1" firstName="Федор" secondName="Иванов" comment="" classId="2"/>
		<element _id="2" firstName="Иван" secondName="Фёдоров" comment="" classId="2"/>
	</learners>
	<learnersOnPlaces>
		<element _id="4" learnerId="1" placeId="2"/>
		<element _id="5" learnerId="2" placeId="11"/>
	</learnersOnPlaces>
	<learnersGrades>
		<element _id="1" date="2022-01-28" lessonNumber="1" grade1="0" grade2="0" grade3="0" title1Id="1" title2Id="1" title3Id="1" absentTypeId="2" subjectId="1" learnerId="1"/>
		<element _id="2" date="2022-01-28" lessonNumber="1" grade1="4" grade2="1" grade3="5" title1Id="1" title2Id="1" title3Id="1" absentTypeId="null" subjectId="1" learnerId="2"/>
	</learnersGrades>
	<subjects>
		<element _id="1" name="Математика" classId="2"/>
		<element _id="3" name="Гейграфия" classId="2"/>
	</subjects>
	<lessonAndTimeWithCabinet>
		<element _id="1" subjectId="1" cabinetId="1" lessonNumber="1" lessonDate="2022-01-28" repeat="0"/>
	</lessonAndTimeWithCabinet>
	<statisticsProfiles/>
	<learnersGradesTitles>
		<element _id="1" title="Устный ответ"/>
	</learnersGradesTitles>
	<learnersAbsentTypes>
		<element _id="1" absentName="Н" absentLongName="Отсутствует"/>
		<element _id="2" absentName="Б" absentLongName="Болеет"/>
	</learnersAbsentTypes>
	<lessonComment>
		<element _id="1" commentLessonId="1" commentDate="2022-01-28" commentText="учить учебник"/>
	</lessonComment>
</school>

* */
